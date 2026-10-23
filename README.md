# TownTalk

> A hyperlocal social feed that classifies every post **on-device**, with no
> server, no API keys, and no network permission.
>
> The demo answers one question: *can a neighbourhood feed label its own posts
> — Emergency / Traffic / Event / Civic / General — entirely on the phone?*

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![TensorFlow Lite](https://img.shields.io/badge/ML-TensorFlow%20Lite-FF6F00?logo=tensorflow&logoColor=white)

---

## What it does

The app reads your position, buckets each post into a stable ~1 km × 1 km grid
cell, and classifies the text **locally** with TensorFlow Lite. As you type in
the composer, the live probability distribution for all five labels updates
after a 250 ms debounce. Publishing stores the winning label and confidence
alongside the post.

If the model assets are missing or the interpreter fails on a given device, a
deterministic keyword scorer takes over — and the UI says so.

---

## The AI model: what it actually is

> [!IMPORTANT]
> **This is a word-embedding MLP, not MobileBERT.**
>
> Earlier revisions of this repo described the bundled model as a fine-tuned
> `google/mobilebert-uncased` transformer. That was incorrect, and the
> difference is not cosmetic — it changes the entire input pipeline.
>
> The model was introspected directly from the FlatBuffer using
> [`ai-edge-litert`](https://pypi.org/project/ai-edge-litert/). Reproduce with
> `tools/inspect_model.py`.

**Measured signature:**

| Property | Actual value |
|---|---|
| Input tensors | **one** — `keras_tensor` |
| Input shape / dtype | `[1, 24]` / `int32` |
| Output tensors | **one** — `output_0` |
| Output shape / dtype | `[1, 5]` / `float32` |
| Signature key | `serving_default` |
| Operators | `Embedding(GatherV2)` → `Dense(ReLU)` → `Dense(BiasAdd)` |
| File size | **592 KB** (a MobileBERT export would be ~25 MB) |
| Vocabulary | `vocab.json` — **578** tokens, specials `<PAD>`=0, `<OOV>`=1 |

**Why the old loader could never work:** it looked for `vocab.txt` and
`max_seq_len.txt` (neither ships), assumed three inputs
(`input_ids`/`attention_mask`/`token_type_ids`), and called
`Interpreter.run(Map, Map)` — which does **not** perform named-tensor lookup.
A second, conflicting path (`LocalClassifier`) fed the same file a 64-dim
float bag-of-words, which is also wrong; it silently fell back to keywords.
The consequence was that **the real model was never used**.

**The pipeline that does work** (`VocabTokenizer` → `runSignature`):

```
"heavy traffic jam on the highway"
  → lowercase, split on non-alphanumerics
  → vocab.json lookup, unknowns → <OOV>, truncate/pad to 24
  → int32[1,24]
  → runSignature("serving_default")
  → float32[1,5]
```

There is no `[CLS]`/`[SEP]`: the model has no segment embeddings, and its
single pooled vector comes straight from the embedding bag.

### Verified predictions

Measured in Python (`tools/probe_semantics.py`) and again on the emulator:

| Input | Predicted | Confidence |
|---|---|---|
| `heavy traffic jam on the highway` | traffic | 0.914 |
| `fire accident rescue needed ambulance` | emergency | 0.999 |
| `street food festival weekend park` | event | 0.477 |
| `garbage pile streetlight pothole water` | civic | 0.669 |
| `hello anyone from the block thanks` | general | 0.729 |

### Honest reporting

The banner at the top of the feed is derived from the loaded model's own
signature, never from a hard-coded string. If the model is unavailable it
distinguishes the cause:

- `MISSING_ASSET` — an asset is not in the APK
- `INITIALIZATION_FAILED` — the interpreter could not be built
- `INFERENCE_FAILED` — the model loaded but a call threw

Each post also stores `classifiedBy`, so a row labelled years ago still reports
the engine that actually produced it.

### Regenerating a genuine MobileBERT model

> [!WARNING]
> **This has not been run.** No MobileBERT assets are bundled, and none were
> invented. If you want a real transformer, this is the recipe — but you must
> execute it yourself and re-verify, because the current app loader is built
> for the embedding model.
>
> `tools/train_locup_classifier.py` and `tools/train_locup_classifier.ipynb`
> describe a MobileBERT fine-tune, but **neither produced the shipped file**.
> Treat them as a starting point, not a verified pipeline.

1. Fine-tune `google/mobilebert-uncased` on your labelled posts.
2. Export with `TFLiteConverter.from_keras_model(model)` (no quantisation, or
   full-integer quantisation if you re-verify accuracy).
3. Confirm **three** int32 inputs `[1, MAX_SEQ_LEN]` and one float output.
4. Ship `vocab.txt` (`[PAD]`/`[UNK]`/`[CLS]`/`[SEP]`), `labels.txt`, and
   `max_seq_len.txt`.
5. Replace `VocabTokenizer` with a WordPiece tokenizer and switch the inference
   call from `runSignature` to a three-input path.
6. Re-run `tools/inspect_model.py` and update the table above.

---

## Architecture

Packages inside the single `app` module — a modular split would be ceremony at
this size.

```
com.locup.mvp
├── domain/                  ← pure Kotlin, no Android, no Room
│   ├── model/               Post, Subscription, GeoPoint, Cluster,
│   │                        PostCategory, InferenceEngine
│   ├── repository/          PostRepository, SubscriptionRepository,
│   │                        TextClassifier, LandmarkResolver  (interfaces)
│   └── usecase/             ObserveFeed, PublishPost
├── data/
│   ├── ai/                  TfliteEmbeddingClassifier, ClassifierAssets,
│   │                        VocabTokenizer, KeywordScorer
│   ├── repository/          RoomPostRepository, RoomSubscriptionRepository
│   ├── entity/ dao/         Room entities, DAOs, database + migration
├── presentation/
│   ├── feed/                FeedScreen (stateless), FeedViewModel, UI state
│   ├── compose/             ComposerSheet (stateless)
│   ├── theme/               TownTalkTheme
│   └── FeedViewModelFactory
├── platform/location/       AndroidLocationProvider (AOSP LocationManager)
└── di/                      AppContainer — manual constructor injection
```

**Key decisions**

- **Domain is dependency-free.** Repository *interfaces* live in `domain`;
  Room/TFLite implementations live in `data`. The dependency arrow points
  inward.
- **One `StateFlow` of one immutable data class** per screen. Separate flows
  would let the UI observe a half-updated state (new filter, stale posts).
- **Stateless Composables.** Screens take state and emit lambdas, so they are
  previewable and testable without a ViewModel.
- **Manual DI, not Hilt.** The graph is small and fully constructor-injected;
  a framework would add build cost and indirection without removing coupling.
- **`INTERNET` permission is deliberately absent**, which makes the offline
  claim verifiable rather than aspirational.

### Coroutines

| Concern | Approach |
|---|---|
| User actions | `viewModelScope` |
| Model load / tokenize / infer | injected background dispatcher |
| Live preview | `debounce(250ms)` → `flatMapLatest` → `map` |
| Stale results | `flatMapLatest` discards an outdated result before it can update state |
| `Interpreter` (not thread-safe) | serialised on a `ReentrantLock` |
| Cancellation | `CancellationException` rethrown, never swallowed |
| Publish | duplicate guard, draft retained on failure, sheet dismissed only on success |

The preview pipeline is derived from state rather than pushed through a
`SharedFlow`. An earlier revision used `tryEmit`, which **silently drops**
values when no collector is subscribed — a bug this refactor removed.

---

## Build and run

**Requirements:** JDK 17, Android SDK with API 34, an emulator or device on
API 24+.

```bash
# from Android Studio: just press Run
# or:
.\gradlew.bat :app:assembleDebug
adb install -r -t app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.locup.mvp/.MainActivity
```

### Verified commands

| Command | Result |
|---|---|
| `.\gradlew.bat :app:assembleDebug` | ✅ BUILD SUCCESSFUL |
| `.\gradlew.bat :app:testDebugUnitTest` | ✅ 48 tests, 0 failures |
| `.\gradlew.bat :app:lintDebug` | ✅ 0 errors (21 warnings) |
| `.\gradlew.bat :app:connectedDebugAndroidTest` | ✅ 4 tests, 0 failures (emulator) |

Test breakdown: `FeedViewModelTest` 17, `VocabTokenizerTest` 11,
`KeywordScorerTest` 10, `ClusterTest` 6, `TableLandmarkResolverTest` 4.
Instrumented: `TfliteEmbeddingClassifierInstrumentedTest` 4.

> [!NOTE]
> **Instrumented tests now cover the real model.** The old
> `TfliteTextClassifierInstrumentedTest` asserted a MobileBERT contract that
> the bundled model does not have, so it could only ever have been skipped or
> failing; it was removed. Its replacement,
> `TfliteEmbeddingClassifierInstrumentedTest`, asserts the *actual* contract
> of the bundled model: the real engine is reported, the model discriminates
> Traffic from Emergency correctly, concurrent `classify()` calls are
> serialised safely, and `classify()` after `close()` degrades to keyword
> scoring instead of crashing. The model's behaviour is therefore verified
> three ways: via `tools/probe_semantics.py` (exact, reproducible), by the
> instrumented test on a device, and manually on the emulator.

### Toolchain note

`gradlew.bat` clears `ANDROID_PREFS_ROOT` before invoking Gradle. On machines
where both `ANDROID_PREFS_ROOT` and `ANDROID_USER_HOME` are exported, AGP
refuses to start with *"Several environment variables ... contain different
paths to the Android Preferences folder"*. Unsetting the deprecated variable is
the documented remedy.

---

## Demo walkthrough

- [DEMO_WALKTHROUGH.md](DEMO_WALKTHROUGH.md) — the 4–5 minute recording script,
  including the exact wording to use and the phrases to avoid.
- [APPLICATION_ANSWERS.md](APPLICATION_ANSWERS.md) — paste-ready answers to a
  job application's questions, a verified-claims table, and a drop-in
  correction for any résumé line that still calls the model MobileBERT.

---

## Limitations

- **Not MobileBERT.** See the model section — this is the honest state.
- **Vocabulary is Hinglish-contaminated** (`hai`, `mein`, `ki`, `gaya`). The
  model was evidently trained on real local posts, so these are legitimate
  in-vocabulary tokens, but English-only input is better served.
- **The live model is only exercised on a device/emulator** — the JVM unit
  tests cannot run the TFLite interpreter (see above).
- **Landmark table is a hardcoded list** of six Bengaluru areas; anything else
  falls back to grid coordinates.
- **Voting is not idempotent per user** — the demo increments a counter with no
  identity to deduplicate against.
- **Single-module** by design; not intended as a template for large teams.