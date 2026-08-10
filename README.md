# LocUp — MobileBERT on Android

> A hyperlocal social feed that classifies every post on-device with a
> fine-tuned **MobileBERT** transformer running through TensorFlow Lite.
> No servers, no API keys, no Play Services.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![TensorFlow Lite](https://img.shields.io/badge/ML-TensorFlow%20Lite-FF6F00?logo=tensorflow&logoColor=white)
![MobileBERT](https://img.shields.io/badge/Model-MobileBERT-FFB000)
![License](https://img.shields.io/badge/License-MIT-blue)

## What it is

LocUp is a tiny Android MVP that explores one idea: *can a
neighbourhood social feed classify its own posts — Emergency / Traffic
/ Event / Civic / General — without ever calling a server?*

The app reads the user's GPS, buckets the post into a stable `~1 km ×
1 km` grid cell, and runs a **MobileBERT fine-tune on the device
CPU/NPU**. The UI shows the live probability distribution for all five
labels as the user types, then publishes the post with the winning
label + confidence attached.

If the TFLite model assets can't be loaded, the classifier transparently
falls back to a deterministic keyword scorer so the end-to-end flow
stays demoable on any device.

---

## Approach — local transformer inference, end to end

```mermaid
flowchart LR
    subgraph Build["Build pipeline (Colab)"]
        CSV[250 labelled posts<br/>50 per category] --> Tk[AutoTokenizer<br/>MobileBERT-uncased]
        Tk --> Mbm[TFAutoModel<br/>google/mobilebert-uncased]
        Mbm --> Head[Dropout + Dense 5]
        Head --> Train[Fine-tune<br/>5 epochs, lr=2e-5]
        Train --> Conv[TFLiteConverter<br/>dynamic-range quant]
        Conv --> A1[locup_text_classifier.tflite<br/>~25 MB]
        Conv --> A2[vocab.txt<br/>~30k tokens]
        Conv --> A3[labels.txt]
        Conv --> A4[max_seq_len.txt]
    end

    subgraph Ship["Shipped in the APK"]
        A1 --> Assets[(app/src/main/assets/)]
        A2 --> Assets
        A3 --> Assets
        A4 --> Assets
    end

    subgraph Runtime["On-device inference (Android)"]
        Compose[ComposeSheet<br/>live keystroke] --> Norm[basicTokenize<br/>NFKD + lowercase + accent strip]
        Norm --> WP[wordpiece<br/>greedy longest-match]
        Assets -.vocab.txt.-> WP
        WP --> Enc[CLS / SEP + pad<br/>IntArray MAX_SEQ_LEN]
        Enc --> Run[Interpreter.run<br/>3 inputs: input_ids,<br/>attention_mask, token_type_ids]
        Run --> Out[FloatArray 5<br/>softmax]
        Out --> Bars[Live probability bars]
    end

    Bars -.fallback if assets missing.-> KW[KeywordFallback.score]
```

**Plain HuggingFace `transformers` on the build side, plain TFLite
`Interpreter` on the device side — no Task Library, no MediaPipe, no
bundled tokenizer metadata.**

The tokenizer is hand-rolled in Kotlin (`BertTokenizer.kt`) and matches
the Python `AutoTokenizer` rules exactly: lowercase + Unicode NFKD +
accent strip, the standard BERT basic-tokenizer regex, then greedy
longest-match WordPiece against the loaded `vocab.txt`. The same
vocab.txt is loaded by Python training and by the Android app, so
input ids line up byte-for-byte.

---

## Build the model

```python
from transformers import AutoTokenizer, TFAutoModel

MODEL_NAME = "google/mobilebert-uncased"
MAX_SEQ_LEN = 64

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME, use_fast=True)
base = TFAutoModel.from_pretrained(MODEL_NAME)

input_ids = tf.keras.Input(shape=(MAX_SEQ_LEN,), dtype=tf.int32, name="input_ids")
attention_mask = tf.keras.Input(shape=(MAX_SEQ_LEN,), dtype=tf.int32, name="attention_mask")
token_type_ids = tf.keras.Input(shape=(MAX_SEQ_LEN,), dtype=tf.int32, name="token_type_ids")
outputs = base(input_ids=input_ids, attention_mask=attention_mask,
               token_type_ids=token_type_ids)
cls = outputs.last_hidden_state[:, 0, :]
x = tf.keras.layers.Dropout(0.1)(cls)
logits = tf.keras.layers.Dense(5, activation="softmax", name="classifier")(x)
model = tf.keras.Model(
    inputs=[input_ids, attention_mask, token_type_ids],
    outputs=logits,
)
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=2e-5),
    loss="sparse_categorical_crossentropy",
    metrics=["accuracy"],
)
model.fit(x, y, validation_data=(xv, yv), epochs=5, batch_size=16)

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]   # dynamic-range quant
tflite_model = converter.convert()
```

The training script lives at `tools/train_locup_classifier.py` (with a
Colab-ready notebook at `tools/train_locup_classifier.ipynb`) and
ships with 250 short, realistic posts (50 per category) — no API
calls, no external datasets. Drop the four assets into
`app/src/main/assets/` and the app picks them up.

---

## Run inference on the device

`PostClassifierRepository` is the single entry point. It owns
`TfliteTextClassifier` (real MobileBERT) and `LocalClassifier` (keyword
fallback) and picks at call time.

```kotlin
class BertTokenizer(vocab: Map<String, Int>, maxSeqLen: Int) {
    fun basicTokenize(text: String): List<String> {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFKD)
            .lowercase()
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        // BERT's basic tokenizer regex: contractions, words, numbers,
        // punctuation, whitespace.
        return BASIC_TOKEN_PATTERN.matcher(normalized).run {
            buildList { while (find()) append(group().trim()) }
        }
    }

    fun wordpiece(token: String): List<String> =
        // Greedy longest-match against the loaded vocab, ## continuation
        // prefix for sub-segments. UNK if no match.
        ...

    fun encode(text: String): TokenizedInput {
        val pieces = basicTokenize(text).flatMap(::wordpiece).take(maxSeqLen - 2)
        // Wrap with [CLS] / [SEP], pad to maxSeqLen, return input_ids +
        // attention_mask (token_type_ids are all zeros).
        ...
    }
}

val t = loaded.tokenizer.encode(text)
val inputIds      = Array(1) { t.inputIds }
val attentionMask = Array(1) { t.attentionMask }
val tokenTypeIds  = Array(1) { IntArray(MAX_SEQ_LEN) }   // single sentence
val output        = Array(1) { FloatArray(5) }
interpreter.run(
    mapOf("input_ids" to inputIds,
          "attention_mask" to attentionMask,
          "token_type_ids" to tokenTypeIds),
    mapOf("classifier" to output),
)
```

Loading uses `context.assets.openFd()` → `FileChannel.map()` → a
memory-mapped `MappedByteBuffer`, so first-inference start-up is
essentially free. If any of the four assets is missing the model
returns `null` from `tryLoad()` and the repository falls back to the
keyword scorer — banner turns green when the real path is active.

---

## Skills demonstrated

**Android**

- Jetpack Compose UI with Material 3 (bottom-sheet composer, live
  probability bars, in-feed chips)
- Room persistence for posts + subscriptions
- AOSP `LocationManager` (no Google Play Services dependency)
- Manual dependency wiring (no DI framework, no Hilt)

**AI / ML**

- End-to-end on-device transformer inference — CSV → HuggingFace
  fine-tune → `TFLiteConverter` with dynamic-range quantisation →
  APK assets → in-app `Interpreter`
- MobileBERT (`google/mobilebert-uncased`) fine-tuned for 5 epochs
  at lr=2e-5 on a custom 5-class dataset
- `TFLiteConverter` with **`tf.lite.Optimize.DEFAULT`** for
  ~4× model size reduction
- 3-input TFLite graph (`input_ids`, `attention_mask`,
  `token_type_ids`) read directly via `Interpreter.run(...)`
- Hand-rolled **WordPiece tokenizer in Kotlin** that mirrors the
  Python `AutoTokenizer` rules exactly:
  - NFKD Unicode normalisation + accent stripping
  - BERT's basic-tokenizer regex (contractions, words, numbers,
    punctuation)
  - Greedy longest-match-first WordPiece with `##` continuation
    prefixes
  - `[CLS]` / `[SEP]` wrapping and `[PAD]` padding
- TFLite `Interpreter` loaded via memory-mapped `FileChannel.map()`
- Graceful fallback to a deterministic keyword scorer when the TFLite
  assets are missing

---

## Build & test

```bash
./gradlew assembleDebug
./gradlew test
```

APK lands in `app/build/outputs/apk/debug/`. The model file is ~25 MB
(dynamic-range quantised), so the APK is correspondingly larger than
a typical tiny demo. Tests cover `Cluster.idFor` geo-bucketing, the
`KeywordFallback` scorer, and the `BertTokenizer` rules.

To regenerate the model:

**Option A — Colab notebook (recommended)**:

Open [tools/train_locup_classifier.ipynb](file:///C:/RAKESH/WORK/Development/LocUp/towntalk/tools/train_locup_classifier.ipynb)
in Colab, switch to a T4 GPU runtime, run all cells. The last cell
downloads a `locup_assets.zip` containing the four assets. Unzip
into `app/src/main/assets/` (and delete the old `vocab.json` if present).

**Option B — Command line**:

```bash
pip install transformers tensorflow
python tools/train_locup_classifier.py
```

## Permissions

- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — attach the
  current `Cluster.id` to a new post (declined permits fall back to a
  demo Bengaluru coordinate).
- `INTERNET` — declared for completeness; **the app does not network
  out**.

---

## Repo layout

```
app/src/main/java/com/locup/mvp
├── MainActivity.kt                            # wires LocationProvider + classifier + repo + Compose tree
├── classifier/
│   ├── PostClassifierRepository.kt             # single entry point: real model or fallback
│   ├── TfliteTextClassifier.kt                 # TFLite Interpreter + 3-input wiring
│   └── BertTokenizer.kt                        # WordPiece tokenizer, mirrors Python AutoTokenizer
├── ml/
│   ├── LocalClassifier.kt                     # TFLite wrapper used by the fallback path
│   └── KeywordFallback.kt                     # deterministic hash-BoW scorer
├── model/
│   ├── Post.kt                                # Cluster.idFor(lat, lon) ~1km grid bucket
│   └── LocationProvider.kt                    # AOSP LocationManager wrapper
├── repo/
│   ├── LocUpRepository.kt                     # facade over DAOs + classifier
│   └── LandmarkRepository.kt                  # lat/lon -> "Near {area}" label
├── data/
│   ├── TownTalkDatabase.kt                    # Room DB (posts + subscriptions)
│   ├── entity/                                # PostEntity, SubscriptionEntity
│   └── dao/                                   # PostDao, SubscriptionDao
└── ui/
    └── LocUpApp.kt                            # Scaffold + feed + ComposeSheet + filter chips
```

Training script: [`tools/train_locup_classifier.py`](file:///C:/RAKESH/WORK/Development/LocUp/towntalk/tools/train_locup_classifier.py)
+ Colab notebook: [`tools/train_locup_classifier.ipynb`](file:///C:/RAKESH/WORK/Development/LocUp/towntalk/tools/train_locup_classifier.ipynb).

---

## License

MIT.
