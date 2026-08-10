#!/usr/bin/env python3
"""
Fine-tune MobileBERT on a 5-class hyperlocal-post task and export:

  app/src/main/assets/locup_text_classifier.tflite   ~25 MB MobileBERT, INT8 quantised
  app/src/main/assets/vocab.txt                       BERT tokenizer vocab (one token per line)
  app/src/main/assets/labels.txt                      class names, one per line, in model output order
  app/src/main/assets/max_seq_len.txt                 single integer, e.g. "64"

Drop these four files into the APK and the on-device
`TfliteTextClassifier` will pick them up. The Kotlin `BertTokenizer`
must mirror the tokenisation rules in this script exactly — see
`tokenize()` and `wordpiece_tokenize()` below.

Run in Colab (T4 GPU recommended):

    !pip install -q transformers tensorflow
    !python tools/train_locup_classifier.py

Why this exists:
    The repo deliberately ships a *real* transformer on-device rather
    than a custom Keras model. The intent is to showcase the
    "fine-tune in Colab, convert to TFLite, run on Android with a
    hand-rolled tokenizer" pipeline end-to-end.

Categories are Emergency / Traffic / Event / Civic / General. The
seed posts below are intentionally written short and realistic for
a hyperlocal feed — not generic NLP benchmarks.
"""

from __future__ import annotations

import os
import pathlib
import random
import re
import unicodedata
from typing import List, Tuple

import numpy as np
import tensorflow as tf
from transformers import AutoTokenizer, TFAutoModel

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

MODEL_NAME = "google/mobilebert-uncased"
MAX_SEQ_LEN = 64
LABELS = ["Emergency", "Traffic", "Event", "Civic", "General"]
NUM_LABELS = len(LABELS)
SEED = 7

random.seed(SEED)
np.random.seed(SEED)
tf.random.set_seed(SEED)

ASSETS_DIR = pathlib.Path("app/src/main/assets")

# ---------------------------------------------------------------------------
# Dataset — 50 short realistic posts per category
# ---------------------------------------------------------------------------
# These are intentionally short, lowercase, and in the register of a
# hyperlocal social feed. No external data, no API calls.

POSTS: List[Tuple[str, str]] = [
    # Emergency (50)
    ("fire on 5th avenue, big smoke, evacuate now", "Emergency"),
    ("car accident at the main junction, need ambulance", "Emergency"),
    ("waterlogging in basement, pump truck please", "Emergency"),
    ("gas leak smell near school, fire dept on the way", "Emergency"),
    ("downed power line sparking on sidewalk, do not approach", "Emergency"),
    ("building collapse on mg road, rescue team needed", "Emergency"),
    ("two-wheeler skid near market, person bleeding", "Emergency"),
    ("short circuit in flat, smoke from switchboard", "Emergency"),
    ("river overflowing, sandbagging at the bridge", "Emergency"),
    ("tree fell on car after storm, driver trapped", "Emergency"),
    ("stranger lurking near kids park, pls watch out", "Emergency"),
    ("medical emergency at the metro station", "Emergency"),
    ("gas cylinder leaked in apartment, fire brigade called", "Emergency"),
    ("car into shop window, glass everywhere", "Emergency"),
    ("child missing in the colony, please help search", "Emergency"),
    ("wall collapse during rains, debris on the road", "Emergency"),
    ("open manhole on the main road, dangerous at night", "Emergency"),
    ("snake spotted in the parking lot, call forest dept", "Emergency"),
    ("stray dog bite near the school gate", "Emergency"),
    ("burning smell from the transformer, lights flickering", "Emergency"),
    ("floodwater entering houses, need rescue boats", "Emergency"),
    ("live wire on the footpath, two people shocked", "Emergency"),
    ("major pileup on the highway, blocking both lanes", "Emergency"),
    ("fire in the high-rise, sprinklers going off", "Emergency"),
    ("ambulance stuck in traffic, please give way", "Emergency"),
    ("robbery at the atm, two suspects on bike", "Emergency"),
    ("building on fire near the hospital", "Emergency"),
    ("flood relief camp accepting donations at the school", "Emergency"),
    ("sewage overflow on the street, unhygienic and unsafe", "Emergency"),
    ("broken glass on the cycle track, cyclist injured", "Emergency"),
    ("gas leak in the restaurant, customers evacuated", "Emergency"),
    ("power cuts everywhere since morning, no eta", "Emergency"),
    ("house flooded up to the doorstep, pls help", "Emergency"),
    ("accident near the school gate, kid hurt", "Emergency"),
    ("smoke from the park, looks like a fire", "Emergency"),
    ("bridge closed, structural damage after the quake", "Emergency"),
    ("two cars collided, airbags deployed, police needed", "Emergency"),
    ("fall from terrace, person unresponsive", "Emergency"),
    ("fire alarm in the mall, please evacuate orderly", "Emergency"),
    ("wells overflowing, kids playing nearby, dangerous", "Emergency"),
    ("rescue operation at the lake, two swimmers missing", "Emergency"),
    ("eye injury from cracker, going to hospital", "Emergency"),
    ("fire in the timber yard, smoke visible from km away", "Emergency"),
    ("stranger trying door handles, please alert", "Emergency"),
    ("road washout after the rain, crater in the middle", "Emergency"),
    ("petrol pump flooded, no fuel available", "Emergency"),
    ("kid fell into the open drain, rescued", "Emergency"),
    ("fire in the electrical room, building evacuated", "Emergency"),
    ("missing elderly person last seen at the temple", "Emergency"),
    ("two-wheeler accident, helmet saved the rider", "Emergency"),

    # Traffic (50)
    ("traffic jam on the highway, moving at snail pace", "Traffic"),
    ("road closed for the marathon, take the bypass", "Traffic"),
    ("signal not working at the main crossing, chaos", "Traffic"),
    ("lane closed for pipe laying, expect delays", "Traffic"),
    ("heavy congestion near the market, leave early", "Traffic"),
    ("traffic police diversion at the school, follow signs", "Traffic"),
    ("vehicle breakdown in the tunnel, single lane", "Traffic"),
    ("roadblock at the bridge, use the alternative route", "Traffic"),
    ("construction work on the flyover, expect 30 min delay", "Traffic"),
    ("metro work blocking the right lane, slow traffic", "Traffic"),
    ("traffic piling up at the toll plaza, cash lanes slow", "Traffic"),
    ("waterlogging under the bridge, slow traffic", "Traffic"),
    ("long queue at the petrol pump, two pumps down", "Traffic"),
    ("protest march on the main road, full diversion", "Traffic"),
    ("container truck breakdown, lane blocked", "Traffic"),
    ("traffic signal cycling green thrice then red, faulty", "Traffic"),
    ("parking full at the mall, do not drive in", "Traffic"),
    ("buses diverted due to the rally, no service on route 12", "Traffic"),
    ("road repair at the station, single lane traffic", "Traffic"),
    ("crane removing the overturned truck, expect 2 hours", "Traffic"),
    ("school pickup causing traffic everywhere at 3pm", "Traffic"),
    ("delivery truck double parked, blocking the lane", "Traffic"),
    ("signal jumped, two wheeler hit, slight jam", "Traffic"),
    ("diversion via 2nd main, two-way traffic on a one-way", "Traffic"),
    ("bumper to bumper from the tech park to the bridge", "Traffic"),
    ("auto strike today, public transport limited", "Traffic"),
    ("metro station crowd spilling onto the road", "Traffic"),
    ("tree branch on the road, slow traffic", "Traffic"),
    ("loud honking at the crossing, classic weekday", "Traffic"),
    ("container fell off the truck, road closed", "Traffic"),
    ("weekend cricket match, road near the ground blocked", "Traffic"),
    ("diversion for the marathon, use the ring road", "Traffic"),
    ("pedestrian crossing blocked by hawkers, traffic slow", "Traffic"),
    ("traffic pile-up at the malfunctioning boom barrier", "Traffic"),
    ("school zone, low speed, expect slow movement", "Traffic"),
    ("work-from-home advisory due to heavy traffic", "Traffic"),
    ("traffic jammed at the new signal, sync not done", "Traffic"),
    ("road widened but no new lane markings, drivers confused", "Traffic"),
    ("no parking on the main road, towing in progress", "Traffic"),
    ("protest rally entering the area, slow traffic", "Traffic"),
    ("traffic from the airport to the city moving fast", "Traffic"),
    ("metro line extension work, two lanes taken", "Traffic"),
    ("overnight road work, single lane now", "Traffic"),
    ("bumper entry restricted today, only residents", "Traffic"),
    ("train timing changed, station road unusually empty", "Traffic"),
    ("flyover opened last night, smooth traffic now", "Traffic"),
    ("bus caught fire near the depot, traffic diverted", "Traffic"),
    ("sensor-based signal not detecting two-wheelers", "Traffic"),
    ("nighttime road closure for the film shoot", "Traffic"),
    ("road caved in, only one lane open", "Traffic"),

    # Event (50)
    ("community yoga session at the park this sunday", "Event"),
    ("weekend farmers market in the society", "Event"),
    ("diwali mela at the club, families welcome", "Event"),
    ("garage sale outside flat 12, 10am to 4pm", "Event"),
    ("garba practice at the community hall, join in", "Event"),
    ("local marathon route through the colony, sunday 6am", "Event"),
    ("blood donation camp at the school, register now", "Event"),
    ("poetry meetup at the cafe, open mic", "Event"),
    ("music concert at the amphitheatre, free entry", "Event"),
    ("food festival at the lakeside, this weekend", "Event"),
    ("holi celebration at the club, colors provided", "Event"),
    ("auction at the community center, vintage items", "Event"),
    ("kite flying contest on the terrace, trophies", "Event"),
    ("festival procession through the main road, 5pm", "Event"),
    ("cricket match between blocks, saturday morning", "Event"),
    ("parenting workshop at the library, free", "Event"),
    ("dance class registrations open at the studio", "Event"),
    ("plant sale at the nursery, weekend only", "Event"),
    ("carrom tournament at the sports complex", "Event"),
    ("trekking group meeting point: parking lot, 6am", "Event"),
    ("holi get-together at the secretary's house", "Event"),
    ("book fair at the school ground for 3 days", "Event"),
    ("open mic night at the brewery, friday", "Event"),
    ("society ganesh chaturthi celebration, all welcome", "Event"),
    ("republic day parade practice, school ground", "Event"),
    ("annual day at the community hall, invite attached", "Event"),
    ("weekend film screening at the amphitheatre", "Event"),
    ("drawing competition for kids at the club", "Event"),
    ("navratri garba at the society ground, 8pm", "Event"),
    ("carols by the lake on christmas eve", "Event"),
    ("new year countdown at the rooftop cafe", "Event"),
    ("valentine's day couples dance at the lounge", "Event"),
    ("society bbq and pool party this saturday", "Event"),
    ("tabla concert at the temple courtyard, evening", "Event"),
    ("photo walk meetup at the metro station, 7am", "Event"),
    ("zumba class launch at the park, free trial", "Event"),
    ("sketching class for beginners at the studio", "Event"),
    ("pet adoption drive at the mall, sunday", "Event"),
    ("community kitchen serving lunch at the temple", "Event"),
    ("open library at the clubhouse, 10am to 6pm", "Event"),
    ("inter-block cricket tournament, finals saturday", "Event"),
    ("hackathon at the tech park, 48 hours", "Event"),
    ("society culturals on the 5th, performances open", "Event"),
    ("monsoon food festival at the lake, enter free", "Event"),
    ("kids science fair at the school, open to all", "Event"),
    ("wine and cheese tasting at the lounge, 7pm", "Event"),
    ("comedy night at the brewery, two shows", "Event"),
    ("early morning cycling meetup at the park", "Event"),
    ("weekend mahjong at the senior citizens club", "Event"),
    ("lantern festival at the lake, evening", "Event"),

    # Civic (50)
    ("garbage pile near the bin, not collected for days", "Civic"),
    ("streetlight not working on the main road, dangerous", "Civic"),
    ("pothole on the main road, two-wheelers skidding", "Civic"),
    ("sewage overflow on the pavement, smells awful", "Civic"),
    ("dump yard overflowing, dogs scattering trash", "Civic"),
    ("open drain on the footpath, kids playing nearby", "Civic"),
    ("broken footpath near the school, accessibility issue", "Civic"),
    ("no water supply in the block since morning", "Civic"),
    ("low water pressure, top floors not getting any", "Civic"),
    ("dirty water coming from the tap, not drinkable", "Civic"),
    ("storm water drain blocked, water stagnating", "Civic"),
    ("public toilet closed for two weeks, no maintenance", "Civic"),
    ("streetlight pole leaning, can fall anytime", "Civic"),
    ("speed breaker unmarked, drivers cant see it", "Civic"),
    ("encroachment on the footpath, no walking space", "Civic"),
    ("mosquito breeding in the stagnant water at the park", "Civic"),
    ("garbage truck missed our street last week", "Civic"),
    ("dhobi ghat drainage choked, water on the road", "Civic"),
    ("illegal parking on the service road, no action", "Civic"),
    ("nala cleaning overdue, blocking the storm water", "Civic"),
    ("zebra crossing paint faded, no visibility", "Civic"),
    ("bus stop bench broken, no shelter from rain", "Civic"),
    ("electric pole wires hanging low, dangerous", "Civic"),
    ("water meter reading wrong, bill inflated", "Civic"),
    ("no dustbins on the main road, people littering", "Civic"),
    ("footpath dug up by the cable operator, no repair", "Civic"),
    ("manhole cover missing, dangerous at night", "Civic"),
    ("public garden not maintained, dead plants", "Civic"),
    ("fallen tree branch not cleared, road narrow", "Civic"),
    ("semi-permanent hawker stalls blocking the lane", "Civic"),
    ("water pump not working in the apartment", "Civic"),
    ("drain smell from the neighbour, not cleaned", "Civic"),
    ("parking lot full of potholes, cars getting damaged", "Civic"),
    ("bus shelter glass broken, no safety", "Civic"),
    ("pavement tiles loose, trip hazard", "Civic"),
    ("dumping of construction debris at the vacant plot", "Civic"),
    ("sewage manhole cover broken, stink all over", "Civic"),
    ("water tanker not coming for the past 3 days", "Civic"),
    ("public taps leaking, wasting water", "Civic"),
    ("kala azar mosquito breeding in the stagnant water", "Civic"),
    ("hand pump broken, no water for the slum", "Civic"),
    ("bus stop on the highway without shelter", "Civic"),
    ("roadside trees not trimmed, blocking the signages", "Civic"),
    ("drain cleaning request ignored for months", "Civic"),
    ("construction dust on the road, no water sprinkling", "Civic"),
    ("pedestrian signal not working at the junction", "Civic"),
    ("waste segregation bins removed by someone", "Civic"),
    ("streetlight timing wrong, dark till late morning", "Civic"),
    ("stray cattle on the road, no action from ward", "Civic"),
    ("parking chaos outside the metro station, no markings", "Civic"),

    # General (50)
    ("hello, anyone from the b block online?", "General"),
    ("hi all, new to the colony, drop a hi", "General"),
    ("thanks for the help yesterday, much appreciated", "General"),
    ("lost my dog near the park, brown labrador, please call", "General"),
    ("found a set of keys near the temple, contact me", "General"),
    ("shoutout to the watchman, very helpful", "General"),
    ("can anyone recommend a good plumber in the area?", "General"),
    ("maid available for part-time work, contact me", "General"),
    ("anyone selling a used washing machine, working condition", "General"),
    ("pet sitting available next week, experienced", "General"),
    ("hello, does anyone know the timings for the post office?", "General"),
    ("hi, looking for a roommate for a 2bhk", "General"),
    ("thanks to the sweeper for cleaning the staircase", "General"),
    ("lost my wallet near the bus stop, please return", "General"),
    ("found a child's cycle near the park, contact me", "General"),
    ("anyone interested in a book exchange at the club", "General"),
    ("recommend a good tiffin service for the area", "General"),
    ("share your evening walks photos in the group", "General"),
    ("good morning, beautiful sunrise from my balcony", "General"),
    ("good night all, see you tomorrow", "General"),
    ("happy birthday to the friendly neighborhood uncle", "General"),
    ("anybody from the c block, lets catch up", "General"),
    ("hi, looking for a carpool to the tech park", "General"),
    ("tiffin service recommendations, only veg", "General"),
    ("anyone selling a study table, used ok", "General"),
    ("hi all, what is the wifi password for the club", "General"),
    ("share your monsoon pictures in the group", "General"),
    ("good morning, tea at the tapri is back", "General"),
    ("hi, which grocery store is open late night", "General"),
    ("recommend a good paediatrician in the area", "General"),
    ("sharing a recipe for soft idlis, let me know results", "General"),
    ("happy diwali everyone, stay safe", "General"),
    ("happy new year, best wishes from our block", "General"),
    ("hi, sharing a heritage walk pdf, useful", "General"),
    ("anyone going to the airport tomorrow, can share", "General"),
    ("found a pair of spectacles near the library", "General"),
    ("hi all, anyone selling a treadmill, working", "General"),
    ("greetings, looking for a yoga instructor for home", "General"),
    ("happy holi, share your photos after playing", "General"),
    ("sharing a poem i wrote on the colony", "General"),
    ("hi all, any local cricket coach available", "General"),
    ("recommend a good salon for haircut", "General"),
    ("happy independence day, flag hoisting at the club", "General"),
    ("found my missing cat, thanks for the help", "General"),
    ("hi all, looking for a used bicycle for my kid", "General"),
    ("recommend a good electrician, work in the kitchen", "General"),
    ("share your terrace garden photos in the group", "General"),
    ("hi all, any plumber available on sunday", "General"),
    ("shoutout to the milk booth, never late", "General"),
    ("hi all, sharing the colony directory pdf", "General"),
    ("thanks for the quick help with the broken pipe", "General"),
]

assert len(POSTS) == 250, f"expected 250 posts, got {len(POSTS)}"
for label in LABELS:
    count = sum(1 for _, l in POSTS if l == label)
    assert count == 50, f"label {label} has {count} posts, expected 50"


# ---------------------------------------------------------------------------
# Tokenization — must match the Kotlin BertTokenizer exactly
# ---------------------------------------------------------------------------

def basic_tokenize(text: str) -> List[str]:
    """Lowercase, strip accents, split on whitespace + punctuation.

    Mirrors `BertTokenizer.basicTokenize` in the Kotlin side.
    """
    text = text.lower()
    text = unicodedata.normalize("NFD", text)
    text = "".join(ch for ch in text if unicodedata.category(ch) != "Mn")
    # BERT basic tokenizer splits on whitespace and these punctuation chars.
    pat = r"'(?:s|ve|re|ll|t|m|d)|[a-z]+|[0-9]+|[^\sa-z0-9]+|\s+"
    tokens = []
    for match in re.finditer(pat, text):
        tok = match.group(0).strip()
        if tok:
            tokens.append(tok)
    return tokens


def wordpiece_tokenize(token: str, vocab: dict, unk_token: str = "[UNK]") -> List[str]:
    """Greedy longest-match-first WordPiece on a single token.

    Mirrors `BertTokenizer.wordpiece` in Kotlin.
    """
    out = []
    start = 0
    n = len(token)
    while start < n:
        end = n
        cur = None
        while start < end:
            sub = token[start:end]
            if start > 0:
                sub = "##" + sub
            if sub in vocab:
                cur = sub
                break
            end -= 1
        if cur is None:
            return [unk_token]
        out.append(cur)
        start = end
    return out


def encode(text: str, vocab: dict, max_seq_len: int, cls_id: int, sep_id: int,
           pad_id: int, unk_id: int) -> Tuple[List[int], List[int]]:
    """Full encode pipeline. Returns (input_ids, attention_mask)."""
    basic = basic_tokenize(text)
    wordpieces: List[str] = []
    for tok in basic:
        wordpieces.extend(wordpiece_tokenize(tok, vocab))

    # Reserve [CLS] and [SEP].
    max_wordpieces = max_seq_len - 2
    wordpieces = wordpieces[:max_wordpieces]

    ids = [cls_id] + [vocab.get(w, unk_id) for w in wordpieces] + [sep_id]
    # Pad.
    while len(ids) < max_seq_len:
        ids.append(pad_id)
    mask = [1 if i < len(ids) and ids[i] != pad_id else 0 for i in range(max_seq_len)]

    # Trim if we somehow exceeded (defensive).
    ids = ids[:max_seq_len]
    mask = mask[:max_seq_len]
    return ids, mask


# ---------------------------------------------------------------------------
# Build the dataset
# ---------------------------------------------------------------------------

def prepare_dataset(tokenizer, texts, labels, max_seq_len):
    """Tokenize with the HF tokenizer (used for training) so we know the
    expected id-mapping. We use the same WordPiece rules as the Kotlin
    tokenizer, but let HF do the heavy lifting for training convenience.
    """
    enc = tokenizer(
        texts,
        max_length=max_seq_len,
        padding="max_length",
        truncation=True,
        return_tensors="tf",
    )
    label_to_idx = {l: i for i, l in enumerate(LABELS)}
    y = np.array([label_to_idx[l] for l in labels], dtype=np.int32)
    return enc["input_ids"], enc["attention_mask"], enc["token_type_ids"], y


def main() -> None:
    print("Loading tokenizer + model...")
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME, use_fast=True)
    base = TFAutoModel.from_pretrained(MODEL_NAME)

    # Build the classification head.
    input_ids = tf.keras.Input(shape=(MAX_SEQ_LEN,), dtype=tf.int32, name="input_ids")
    attention_mask = tf.keras.Input(shape=(MAX_SEQ_LEN,), dtype=tf.int32, name="attention_mask")
    token_type_ids = tf.keras.Input(shape=(MAX_SEQ_LEN,), dtype=tf.int32, name="token_type_ids")
    outputs = base(
        input_ids=input_ids,
        attention_mask=attention_mask,
        token_type_ids=token_type_ids,
    )
    cls = outputs.last_hidden_state[:, 0, :]
    x = tf.keras.layers.Dropout(0.1)(cls)
    logits = tf.keras.layers.Dense(NUM_LABELS, activation="softmax", name="classifier")(x)
    model = tf.keras.Model(
        inputs=[input_ids, attention_mask, token_type_ids],
        outputs=logits,
    )

    # ~9/10 train, 1/10 val.
    rng = np.random.default_rng(SEED)
    idx = np.arange(len(POSTS))
    rng.shuffle(idx)
    split = int(0.9 * len(POSTS))
    train_idx, val_idx = idx[:split], idx[split:]

    texts = [p[0] for p in POSTS]
    labels = [p[1] for p in POSTS]
    train_texts = [texts[i] for i in train_idx]
    train_labels = [labels[i] for i in train_idx]
    val_texts = [texts[i] for i in val_idx]
    val_labels = [labels[i] for i in val_idx]

    x_train_ids, x_train_mask, x_train_tti, y_train = prepare_dataset(
        tokenizer, train_texts, train_labels, MAX_SEQ_LEN)
    x_val_ids, x_val_mask, x_val_tti, y_val = prepare_dataset(
        tokenizer, val_texts, val_labels, MAX_SEQ_LEN)

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=2e-5),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )

    print("Training (5 epochs)...")
    model.fit(
        x=[x_train_ids, x_train_mask, x_train_tti],
        y=y_train,
        validation_data=([x_val_ids, x_val_mask, x_val_tti], y_val),
        epochs=5,
        batch_size=16,
        verbose=2,
    )

    # Quick sanity-check accuracy on the full set.
    x_all_ids, x_all_mask, x_all_tti, y_all = prepare_dataset(
        tokenizer, texts, labels, MAX_SEQ_LEN)
    preds = model.predict([x_all_ids, x_all_mask, x_all_tti], verbose=0).argmax(axis=1)
    train_acc = (preds == y_all).mean()
    print(f"  full-set accuracy (training + val): {train_acc:.3f}")

    # -----------------------------------------------------------------------
    # Convert to TFLite with dynamic-range quantization.
    # -----------------------------------------------------------------------
    print("Converting to TFLite (dynamic-range quant)...")
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()

    # -----------------------------------------------------------------------
    # Export assets.
    # -----------------------------------------------------------------------
    ASSETS_DIR.mkdir(parents=True, exist_ok=True)

    model_path = ASSETS_DIR / "locup_text_classifier.tflite"
    model_path.write_bytes(tflite_model)
    print(f"wrote {model_path} ({model_path.stat().st_size:,} bytes)")

    vocab_path = ASSETS_DIR / "vocab.txt"
    vocab_path.write_text(tokenizer.vocab, encoding="utf-8")
    print(f"wrote {vocab_path} ({vocab_path.stat().st_size:,} bytes, "
          f"{len(tokenizer.vocab):,} tokens)")

    labels_path = ASSETS_DIR / "labels.txt"
    labels_path.write_text("\n".join(LABELS) + "\n", encoding="utf-8")
    print(f"wrote {labels_path} ({labels_path.stat().st_size:,} bytes)")

    seq_len_path = ASSETS_DIR / "max_seq_len.txt"
    seq_len_path.write_text(str(MAX_SEQ_LEN) + "\n", encoding="utf-8")
    print(f"wrote {seq_len_path} ({seq_len_path.stat().st_size:,} bytes)")

    print("\nDone. Drop the assets folder into the APK and the device-side")
    print("TfliteTextClassifier will pick up the MobileBERT model.")


if __name__ == "__main__":
    main()
