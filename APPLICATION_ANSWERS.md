# TownTalk — application answers & recording script

Prepared for the **Senior Android Engineer (Kotlin, Bengaluru)** application.

> [!IMPORTANT]
> **The model is not MobileBERT.** Say *"on-device text classification with
> TensorFlow Lite"* and, when you show the banner, *"TFLite word-embedding
> MLP"*. Those names are verified on-device (see *Verified claims* below).
> The earlier résumé line claiming MobileBERT must be corrected — a drop-in
> replacement is in the last section.

> [!NOTE]
> **This is a personal prototype**, built to demonstrate Android engineering
> judgement. It is not production work and has no backend, no auth, and no
> real users. Say so plainly. Royal Caribbean / TellM are separate examples
> of production experience — the placeholders below mark where to reference
> them with your own specifics.

---

## 1. The problem the app solves, and my contribution

> "TownTalk is a small personal prototype I built to answer one question:
> can a neighbourhood feed label its own posts — Emergency, Traffic, Event,
> Civic, General — entirely on the phone, with no server and no network
> permission?
>
> My contribution is the whole thing: the architecture, the on-device
> inference path, and the tests. I used AI assistants during development for
> code generation and for reviewing my concurrency design, but I verified
> every claim myself — I ran the model contract through an independent
> tool, wrote the concurrency regression test, and confirmed the app
> behaviour on an emulator.
>
> The interesting part for me wasn't the model. It was that the model
> everyone assumed was there — including me, at first — wasn't actually
> running."

**On-camera:** show the feed with the five sample posts, then the banner.

---

## 2. Technologies and architecture

> "Kotlin, Jetpack Compose with Material 3, Room, coroutines and Flow,
> and TensorFlow Lite. No Hilt, no DI framework.
>
> It's a single Gradle module, but with real layer boundaries inside it.
> The domain package is pure Kotlin — no Android imports, no Room
> annotations. Repository *interfaces* live in domain; the Room and TFLite
> implementations live in data. The UI is stateless Composables over a
> single immutable StateFlow per screen.
>
> The dependency arrow points inward, and I can unit test the whole domain
> layer on the JVM without Robolectric."

**On-camera:** Android Studio → `domain/repository/PostRepository.kt`, then
`data/repository/RoomPostRepository.kt`, then `di/AppContainer.kt`.

---

## 3. Key technical trade-offs and my reasons

> "Four trade-offs I'd defend.
>
> **One: a single StateFlow of one data class per screen, instead of several
> independent flows.** Several flows let the UI observe a half-updated state
> — the new filter with the previous post list. One immutable snapshot makes
> that impossible.
>
> **Two: manual DI instead of Hilt.** The graph is small and entirely
> constructor-injected. `AppContainer` is about fifty lines and it makes the
> dependency list for the screen visible in one place. Hilt would add build
> cost and indirection without removing any real coupling.
>
> **Three: live classification on a debounce, not on every keystroke.**
> Tokenising and running inference on each character would burn battery for
> no benefit. Two hundred and fifty milliseconds collapses a burst of
> keystrokes into one inference.
>
> **Four — and this is the one I'd most like to talk about: offline privacy
> versus model capability.** Running inference on-device means a user's post
> never leaves the phone, which matters for location data. But the model I
> could ship and verify is a small word-embedding classifier with a 578-token
> vocabulary, not a transformer. I chose to ship the smaller model that
> genuinely works and describe it honestly, rather than claim a transformer
> I hadn't actually run. The README documents the exact steps to build a real
> MobileBERT version — but I haven't executed them, so I don't claim it.
>
> That trade-off is also why the banner derives its text from the loaded
> model's own signature instead of a hard-coded string: the UI structurally
> cannot lie about which engine is running."

**On-camera:** show `TfliteEmbeddingClassifier.kt` — the banner summary
built from the introspected signature.

---

## 4. What I delivered and the outcome

> "A working local prototype. The complete path works end to end: type a
> post, it's classified on-device in real time with the full probability
> distribution, you publish it, it's stored in Room, and it survives an app
> restart. Voting, filtering and area subscriptions are all wired to the
> same reactive stream.
>
> Verification: forty-eight unit tests, four instrumented tests against the
> real model on an emulator, zero lint errors, and a successful debug build.
>
> One example I'd point to: the instrumented concurrency test. I had a
> thread-safety bug where inference wasn't taking the lock protecting the
> TFLite interpreter. I proved the test caught it by temporarily reverting
> the fix and watching the native runtime fail with a gather-index error —
> so that test genuinely fails when the bug is present, rather than just
> passing."

**On-camera:** the test-results panel, then the running app.

---

## 5. Loom link

> Placeholder — insert your Loom URL here after recording.
> Verify it opens in a private/incognito window before submitting.

---

# Recording script (4–5 minutes)

> [!IMPORTANT]
> **Say "on-device text classification with TensorFlow Lite"**, and when the
> banner is visible, **"TFLite word-embedding MLP"**.
>
> **Never say:** "MobileBERT", "transformer", "fine-tuned BERT", "BERT
> tokenizer".
>
> **On cancellation:** say *"debouncing limits how often inference runs, and
> `flatMapLatest` guarantees an outdated result never updates the UI."*
> Do **not** say cancellation stops the inference — the native call is
> blocking and runs to completion; it is the *result* that gets discarded.

| Time | Show on screen | Spoken line (short) |
|---|---|---|
| **0:00–0:30** | App feed, five sample posts, banner visible | "TownTalk is a personal prototype: a neighbourhood feed that classifies posts on-device. No server, and the app doesn't even request internet permission." |
| | Banner zoom | "This banner is the engine that actually ran — it prints the model's real tensor signature, so the UI can't lie about what's running." |
| **0:30–1:30** | Tap **Post**, type `heavy traffic jam on the highway` | "As I type, the label updates after a 250 ms debounce — so a burst of keystrokes produces one inference, not one per character." |
| | Live distribution: **Traffic 91%** | "That's a real TensorFlow Lite inference on the device. The whole distribution is shown, not just the winner." |
| | Tap **Publish** | "Publishing classifies, stores the label and confidence in Room, and closes the composer only after the write succeeds." |
| | Post at top of feed | "The new post is at the top, tagged Traffic." |
| | Upvote a post | "Voting is one SQL update. Room's Flow re-emits, so the count updates reactively — no manual refresh." |
| | Tap **Near me** / **Following** / **All** | "Filters are pure operators over the same stream — switching doesn't reload anything." |
| | **Force-stop and relaunch** the app | "And it's persisted: I'll kill the app completely and relaunch." |
| | Post still there | "Still there — Room-backed." |
| **1:30–2:30** | Android Studio → `FeedViewModel.kt` | "Here's the whole action path. The ViewModel exposes one immutable StateFlow — `FeedUiState`." |
| | `PublishPost.kt` | "`PublishPost` owns one product decision: with no GPS fix, fall back to sample coordinates so the app still works." |
| | `ObserveFeed.kt` | "`ObserveFeed` combines posts, subscriptions and position into the visible feed." |
| | `domain/repository/PostRepository.kt` | "The ViewModel depends on this interface, not on Room." |
| | `data/repository/RoomPostRepository.kt` | "This is the implementation — it maps entities to domain models." |
| **2:30–3:30** | `TfliteEmbeddingClassifier.kt` | "This is the on-device engine. The interpreter isn't thread-safe, so inference takes a lock." |
| | The `infer()` lock block | "I had a bug here — inference wasn't taking the lock. The instrumented test now covers it." |
| | `VocabTokenizer.kt` | "The tokenizer matches the model's actual contract: 24 token ids, unknowns map to OOV." |
| | `KeywordScorer.kt` | "And if the model can't load, a deterministic keyword scorer takes over — and the UI says so." |
| **3:30–4:15** | App + `AppContainer.kt` | "Manual DI — the graph is small and fully constructor-injected." |
| | `domain/` tree | "Domain is pure Kotlin. No Android, no Room." |
| | Manifest | "No INTERNET permission — the offline claim is verifiable, not aspirational." |
| **4:15–4:45** | Test results, then the app | "Forty-eight unit tests, four instrumented tests against the real model, zero lint errors." |
| | Limitations | "The limitation is the model: it's a small embedding classifier, not a transformer. The README documents how to build a real MobileBERT version — but I haven't run those steps, so I'm not claiming it." |
| | Closing | "[FILL IN: one sentence connecting this to TellM and your Android experience.]" |

---

## Anticipated questions

**"Is that really running on the device?"**
Yes. The banner prints the model's introspected signature
(`int32[1,24] -> float32[1,5]`, 578 tokens), and I reproduce the same
predictions independently in Python against the same `.tflite` file.

**"Why not a bigger model?"**
Size, accuracy on a small vocabulary, and — honestly — I can only claim what
I've actually verified running.

**"How would you test this further?"**
A real instrumented suite covering Room migrations and the classifier against
a re-exported model. The one I'd prioritise is a property test that the
banner's engine always matches the engine that produced the last
classification — that invariant is what caught the original bug.

**"How did AI assist?"**
For code generation and design review. I verified every claim independently
— model contract via a separate tool, concurrency via a regression test I
proved fails without the fix.

---

# Résumé correction

> [!WARNING]
> Replace any MobileBERT claim with the wording below. The original claim was
> factually wrong: the shipped model is a word-embedding MLP, and the loader
> that claimed otherwise could never have run.

**Replace:**

> ~~Classifies posts on-device using a fine-tuned MobileBERT transformer via
> TensorFlow Lite.~~

**With:**

> **On-device text classification with TensorFlow Lite.** Built and verified a
> complete offline inference pipeline for a hyperlocal feed: introspected the
> bundled TFLite model's real tensor contract, implemented a matching
> tokenizer and signature-based inference, and repaired a thread-safety defect
> in the interpreter access path (covered by an instrumented regression test
> that fails without the fix). The app ships a deterministic keyword fallback
> and reports which engine actually produced every label.

**Also remove or correct:** any résumé bullet claiming a
"fine-tuned `google/mobilebert-uncased`" model, a ~25 MB asset, a BERT
`vocab.txt`, or a three-input `input_ids`/`attention_mask`/`token_type_ids`
signature. None of those are true of this repository.

---

# Verified claims

Every claim above was checked on the emulator (`Medium_Phone`, API 37) against
the running app, not inferred from the source.

| Claim | How it was verified | Result |
|---|---|---|
| Engine name shown in the UI | Read from the live `uiautomator` dump | `Classified on-device by TFLite word-embedding MLP` |
| Model signature | Banner text rendered from the loaded model | `int32[1,24] -> float32[1,5], 578 tokens` |
| Traffic classification | Typed the demo text, read the live distribution | **Traffic 91%** |
| Publish persists | `force-stop`, relaunch, re-read the feed | Post still present |
| Vote updates | Tapped upvote, re-read the feed | Count incremented |
| Builds and tests pass | `assembleDebug`, `testDebugUnitTest`, `lintDebug`, `connectedDebugAndroidTest` | All green |
| Thread-safety fix | Regression test run with the fix temporarily reverted | Failed with a native `gather index out of bounds` — so the test genuinely detects the bug |

> [!NOTE]
> **What I could not verify:** real-device behaviour on physical handsets
> (only an emulator was available), performance under sustained typing on
> low-end hardware, and the MobileBERT upgrade path, which is documented but
> not executed.
