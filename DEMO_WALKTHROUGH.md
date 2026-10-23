# TownTalk — demo walkthrough (4–5 minutes)

A script for a screen recording. Timings are cumulative; the whole thing fits
in roughly four and a half minutes.

> [!IMPORTANT]
> **Naming.** The bundled model is a **word-embedding MLP**, not a
> transformer. Say *"on-device text classification with TensorFlow Lite"*, and
> when the banner is on screen, *"TFLite word-embedding MLP"*.
>
> **Never say:** MobileBERT, transformer, fine-tuned BERT, BERT tokenizer.
> Those names belonged to an earlier, incorrect claim — see the README's
> model section for why.

> [!NOTE]
> **On cancellation.** Say *"debouncing limits how often inference runs, and
> `flatMapLatest` guarantees an outdated result never updates the UI."*
> Do **not** claim cancellation stops the inference: `classify()` is a
> blocking native call, so it runs to completion and it is the *result* that
> gets discarded.

> [!NOTE]
> This is a **personal prototype**. Say so. Royal Caribbean and TellM are
> separate examples of production experience.

---

## 0:00–0:30 — The feed and the problem (30s)

**Show:** the feed, five sample posts, the classifier banner at the top.

> "TownTalk is a personal prototype I built to answer one question: can a
> neighbourhood feed label its own posts — Emergency, Traffic, Event, Civic,
> General — entirely on the phone, with no server and no network permission?
>
> My contribution is the whole thing: the architecture, the on-device
> inference path, and the tests. I used AI assistants for code generation
> and design review, but I verified every claim myself — I checked the model
> contract with an independent tool, wrote the concurrency regression test,
> and confirmed the behaviour on an emulator."

**Show:** point at the banner.

> "That banner is the engine that actually ran. It prints the model's own
> tensor signature, so the UI structurally cannot lie about what's running."

---

## 0:30–1:30 — The complete feature: create and classify a post (60s)

**Show:** tap **Post**, type `heavy traffic jam on the highway`.

> "As I type, the label updates after a 250 millisecond debounce — a burst
> of keystrokes produces one inference, not one per character."

**Show:** the live distribution — **Traffic 91%**.

> "That's a real TensorFlow Lite inference on the device, and it shows the
> full distribution rather than just the winner."

**Show:** tap **Publish**.

> "Publishing classifies, stores the label and confidence in Room, and closes
> the composer only after the write actually succeeds. If it failed, the
> draft would stay put."

**Show:** the post at the top of the feed.

> "It's at the top, tagged Traffic."

**Show:** upvote a post.

> "Voting is one SQL update. Room's Flow re-emits, so the count updates
> reactively — there's no manual refresh anywhere in the app."

**Show:** tap **Near me**, **Following**, **All**.

> "Filters are pure operators over the same stream. Switching doesn't reload
> anything."

**Show:** `adb shell am force-stop` then relaunch (or just relaunch).

> "And it's persisted. I'll kill the app completely."

**Show:** the post is still there.

> "Still there — Room-backed."

---

## 1:30–2:30 — Architecture: one action, end to end (60s)

**Show:** `app/src/main/java/com/locup/mvp/presentation/feed/FeedViewModel.kt`

> "Here's the whole path. The ViewModel exposes one immutable StateFlow —
> `FeedUiState`. Not several flows, because several flows let the UI observe
> a half-updated state: the new filter with the previous post list."

**Show:** `domain/usecase/PublishPost.kt`

> "`PublishPost` owns one product decision — with no GPS fix, fall back to
> sample coordinates so the app stays usable without location permission."

**Show:** `domain/usecase/ObserveFeed.kt`

> "`ObserveFeed` combines posts, subscriptions and position into the visible
> feed."

**Show:** `domain/repository/PostRepository.kt`, then
`data/repository/RoomPostRepository.kt`

> "The ViewModel depends on this interface, never on Room. This is the
> implementation, and it maps entities into domain models."

---

## 2:30–3:30 — The on-device AI pipeline (60s)

**Show:** `data/ai/TfliteEmbeddingClassifier.kt` — the lock in `infer()`.

> "This is the on-device engine. The TFLite interpreter isn't thread-safe, so
> inference takes a lock."

**Show:** the `infer()` block and the `closed` check.

> "I had a bug here — inference wasn't taking the lock, so two concurrent
> classifications could enter the native runtime together. I proved the
> regression test catches it by temporarily reverting the fix and watching
> the native runtime fail with a gather-index error."

**Show:** `data/ai/VocabTokenizer.kt`

> "The tokenizer matches the model's actual contract: 24 token ids, unknown
> words map to OOV, and there's no CLS/SEP because this model has no segment
> embeddings."

**Show:** `data/ai/KeywordScorer.kt`

> "And if the model can't load, a deterministic keyword scorer takes over —
> and the banner tells you that's what happened."

---

## 3:30–4:15 — Trade-offs (45s)

**Show:** `di/AppContainer.kt`

> "Manual DI rather than Hilt. The graph is small and entirely
> constructor-injected, so the dependency list for the screen is visible in
> one place."

**Show:** the `domain/` package tree.

> "The domain layer is pure Kotlin — no Android imports, no Room. I can unit
> test all of it on the JVM without Robolectric."

**Show:** the manifest.

> "There's no INTERNET permission. The offline claim is verifiable rather
> than aspirational."

**Speak (trade-off to land):**

> "The trade-off I'd most like to talk about is offline privacy versus model
> capability. Running inference on-device means a user's post never leaves
> the phone, which matters when the post carries location data. But the model
> I could ship *and verify* is a small word-embedding classifier, not a
> transformer. I chose to ship the smaller model that genuinely works and
> describe it honestly, rather than claim a transformer I hadn't actually
> run. The README documents the exact steps to build a real MobileBERT
> version — but I haven't executed them, so I don't claim it."

---

## 4:15–4:45 — Outcome, limitations, close (30s)

**Show:** the test results panel, then the running app.

> "Forty-eight unit tests, four instrumented tests against the real model on
> an emulator, zero lint errors, successful debug build.
>
> The limitation is the model size, and the fact that this is a prototype —
> no backend, no auth, one landmark table hardcoded.
>
> [FILL IN: one sentence connecting this to TellM and your Android
> experience.]"

---

## Anticipated questions

**"Is that really running on the device?"**
Yes — the banner prints the model's introspected signature
(`int32[1,24] -> float32[1,5]`, 578 tokens), and the same predictions
reproduce independently in Python against the same `.tflite` file.

**"Why not a bigger model?"**
Size, accuracy on a small vocabulary, and — honestly — I only claim what I've
verified running.

**"How would you test it further?"**
Room migration tests, and a property test asserting the banner's engine
always matches the engine that produced the last classification. That
invariant is what caught the original bug.

**"How did AI assist?"**
Code generation and design review. I verified every claim independently —
model contract via a separate tool, concurrency via a regression test proven
to fail without the fix.
