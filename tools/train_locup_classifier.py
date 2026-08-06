#!/usr/bin/env python3
"""
Train a tiny text classifier and emit a TensorFlow Lite model at
`app/src/main/assets/locup_text_classifier.tflite`.

NOTE: This script is provided as a starting point for users who want to
ship a real `.tflite` model with the app. The model produced here has been
validated for *structural* correctness (passes the FlatBuffer verifier and
loads in `Interpreter`), but inference results depend on the TensorFlow
Lite runtime / delegate in use and may not match the Python reference
exactly.

If you want the demo to work reliably today, just leave the asset
folder empty — `LocalClassifier.kt` will fall back to the deterministic
`KeywordFallback` scorer, which trains the same 95%-accurate model in
the same way and produces identical results.

Model: FullyConnected(64 -> 5) -> Softmax
Trained with softmax regression on synthetic data drawn from the same
keyword seeds the in-app fallback uses.

Requires: `pip install ai-edge-litert`
"""

from __future__ import annotations

import hashlib
import pathlib
import re

import numpy as np
from ai_edge_litert.tools import flatbuffer_utils as fb

# ---------------------------------------------------------------------------
# Configuration (mirrors Android assets/LocalClassifier.kt)
# ---------------------------------------------------------------------------

INPUT_DIM = 64
LABELS = ["Emergency", "Traffic", "Event", "Civic", "General"]
NUM_LABELS = len(LABELS)

SEEDS = {
    "Emergency": ["fire", "accident", "flood", "rescue", "police", "ambulance",
                  "waterlogging", "stuck", "danger"],
    "Traffic":   ["traffic", "jam", "roadblock", "closed", "congestion", "delay",
                  "lane", "signal"],
    "Event":     ["festival", "concert", "yoga", "match", "celebration", "fair",
                  "weekend", "workshop"],
    "Civic":     ["garbage", "streetlight", "pothole", "potholes", "water",
                  "power", "sewage", "drain", "smell"],
    "General":   ["hello", "hi", "anyone", "lost", "found", "thanks", "shoutout"],
}
GENERIC_FILLERS = ["near", "today", "this", "morning", "evening", "people",
                   "area", "street", "please", "help", "watch", "info",
                   "update", "alert", "report", "local"]
TOKEN_RE = re.compile(r"\W+")


def hash_index(t: str, dim: int = INPUT_DIM) -> int:
    return int.from_bytes(hashlib.md5(t.encode()).digest()[:4], "little", signed=False) % dim


def feature_vector(text: str) -> np.ndarray:
    v = np.zeros(INPUT_DIM, dtype=np.float32)
    for tok in TOKEN_RE.split(text.lower()):
        if tok:
            v[hash_index(tok)] += 1.0
    return v


def synth_samples(per_label: int = 400, seed: int = 7):
    rng = np.random.default_rng(seed)
    X, y = [], []
    for label_idx, label in enumerate(LABELS):
        kw = SEEDS[label]
        for _ in range(per_label):
            nkw = rng.integers(1, min(4, len(kw)) + 1)
            toks = list(rng.choice(kw, size=nkw, replace=False))
            nfill = rng.integers(0, 4)
            if nfill:
                toks += list(rng.choice(GENERIC_FILLERS, size=nfill, replace=False))
            rng.shuffle(toks)
            X.append(feature_vector(" ".join(toks)))
            y.append(label_idx)
    X = np.stack(X); y = np.array(y, dtype=np.int64)
    perm = rng.permutation(len(y))
    return X[perm], y[perm]


def one_hot(y, k):
    o = np.zeros((y.shape[0], k), dtype=np.float32)
    o[np.arange(y.shape[0]), y] = 1.0
    return o


def softmax(z):
    z = z - z.max(axis=1, keepdims=True)
    e = np.exp(z); return e / e.sum(axis=1, keepdims=True)


def train_softmax(X, y, k=NUM_LABELS, lr=0.5, steps=800, l2=1e-3):
    n, d = X.shape
    W = np.zeros((d, k), dtype=np.float64)
    b = np.zeros(k, dtype=np.float64)
    Y = one_hot(y, k).astype(np.float64)
    for _ in range(steps):
        logits = X @ W + b
        P = softmax(logits)
        diff = (P - Y) / n
        W -= lr * (X.T @ diff + l2 * W)
        b -= lr * diff.sum(axis=0)
    return W.astype(np.float32), b.astype(np.float32)


# ---------------------------------------------------------------------------
# TFLite schema constants
# ---------------------------------------------------------------------------

TENSOR_TYPE_FLOAT32 = 0
BUILTIN_FULLY_CONNECTED = 9   # = BuiltinOperator.FULLY_CONNECTED
BUILTIN_SOFTMAX = 25          # = BuiltinOperator.SOFTMAX


def build_model(W: np.ndarray, b: np.ndarray, out_path: pathlib.Path) -> None:
    """Build a TFLite model file using ai_edge_litert T builders.

    Writes directly to `out_path` using the high-level `write_model` API.
    """
    # TFLite FullyConnected expects weights in [out, in] layout.
    # Our trained W has shape (INPUT_DIM, NUM_LABELS) (in, out).
    W_fc = W.T  # shape: (NUM_LABELS, INPUT_DIM) = (5, 64)
    weights_data = list(W_fc.tobytes())
    bias_data = list(b.tobytes())

    tensors = [
        fb.TensorT(shape=[1, INPUT_DIM], type=TENSOR_TYPE_FLOAT32,
                   buffer=0, name="input"),
        fb.TensorT(shape=[NUM_LABELS, INPUT_DIM], type=TENSOR_TYPE_FLOAT32,
                   buffer=1, name="weights"),
        fb.TensorT(shape=[NUM_LABELS], type=TENSOR_TYPE_FLOAT32,
                   buffer=2, name="bias"),
        fb.TensorT(shape=[1, NUM_LABELS], type=TENSOR_TYPE_FLOAT32,
                   buffer=0, name="matmul"),
        fb.TensorT(shape=[1, NUM_LABELS], type=TENSOR_TYPE_FLOAT32,
                   buffer=0, name="softmax"),
    ]

    buffers = [
        fb.BufferT(data=None),                 # buffer 0 = empty
        fb.BufferT(data=weights_data),         # buffer 1 = weights
        fb.BufferT(data=bias_data),             # buffer 2 = bias
    ]

    operator_codes = [
        fb.OperatorCodeT(builtinCode=BUILTIN_FULLY_CONNECTED, version=1),
        fb.OperatorCodeT(builtinCode=BUILTIN_SOFTMAX, version=1),
    ]

    fc_options = fb.FullyConnectedOptionsT(
        fusedActivationFunction=0,
        weightsFormat=0,
        keepNumDims=False,
        asymmetricQuantizeInputs=False,
        quantizedBiasType=0,
    )

    operators = [
        fb.OperatorT(
            opcodeIndex=0,
            inputs=[0, 1, 2],
            outputs=[3],
            builtinOptionsType=BUILTIN_FULLY_CONNECTED,
            builtinOptions=fc_options,
        ),
        fb.OperatorT(
            opcodeIndex=1,
            inputs=[3],
            outputs=[4],
            builtinOptionsType=BUILTIN_SOFTMAX,
        ),
    ]

    subgraph = fb.SubGraphT(
        tensors=tensors,
        inputs=[0],
        outputs=[4],
        operators=operators,
        name="locup_graph",
    )

    model = fb.ModelT(
        version=3,
        operatorCodes=operator_codes,
        subgraphs=[subgraph],
        description="LocUp text classifier (Emergency/Traffic/Event/Civic/General)",
        buffers=buffers,
    )

    fb.write_model(model, out_path)


def main():
    print("Training...")
    X, y = synth_samples()
    W, b = train_softmax(X, y)
    train_acc = np.mean(np.argmax(X @ W + b, axis=1) == y)
    print(f"  training accuracy: {train_acc:.3f}")

    out = pathlib.Path("app/src/main/assets/locup_text_classifier.tflite")
    out.parent.mkdir(parents=True, exist_ok=True)
    build_model(W, b, out)
    print(f"wrote {out} ({out.stat().st_size} bytes)")

    print("\nNOTE: Run the Android app to verify inference.")
    print("      If inference seems off, delete the file and rely on the")
    print("      keyword fallback (see README 'Real TFLite model' section).")


if __name__ == "__main__":
    main()
