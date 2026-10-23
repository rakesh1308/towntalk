# TownTalk — 3–5 minute demo walkthrough

A script for a screen recording. Timings are cumulative; the whole thing fits
in four minutes.

---

## 0:00 — What this is (20s)

> "TownTalk is a hyperlocal feed that labels every post on-device. No server,
> no API keys — the app doesn't even request internet permission."

Open on the feed. Five sample posts, each badged **Sample post**.

> "Everything you see is stored in Room and rendered by Compose. Let me show
> you the flow."

---

## 0:20 — The feed (40s)

**Filters.** Tap *Near me*, *Following*, *All*.

> "The feed is driven by three sources combined in one Flow — posts,
> subscriptions, and my position. Switching filters doesn't reload anything;
> it's a pure operator over the same stream."

**Voting.** Tap an upvote.

> "Upvote is a single SQL UPDATE that bumps a counter. Room's Flow notices and
> re-emits, so the number updates without any manual refresh."

> [!TIP]
> Worth pointing at: the badge shows **two different signals** — "Community +7"
> (crowd) and "model 76%" (the classifier). Keeping them separate is
> deliberate; a confident model can still be wrong.

---

## 1:00 — On-device classification (90s)

Tap **Post**, then type slowly: `heavy traffic jam on the highway`

> "As I type, the label updates after a 250 millisecond debounce. Three
> keystrokes inside that window produce one inference, not three."

**Pause on the result: Traffic 91%.**

> "That's real TensorFlow Lite inference — the whole model runs on the phone.
> The banner at the top shows the engine that actually produced this:
> *TFLite word-embedding MLP*, int32[1,24] into float32[1,5], 578 tokens."

> [!IMPORTANT]
> This is the strongest moment in the demo. The banner is derived from the
> loaded model's own signature — it is **not** a hard-coded string. Earlier
> versions of this project claimed "MobileBERT" while actually running a
> keyword scorer; the whole point of the fix is that the UI cannot lie about
> which engine is live.

Tap **Publish**.

> "The sheet closes only after the row is actually persisted. If the write
> fails, the draft stays put and the error is shown — you never lose what you
> typed."

**Post appears at the top of the feed**, classified Traffic.

---

## 2:30 — Concurrency design (45s)

> "Three coroutine decisions worth calling out."

1. **Live preview** — `debounce` then `flatMapLatest`. If I type fast, the
   in-flight classification for the older text is *cancelled*, so a slow
   result can never overwrite a newer one.
2. **The TFLite `Interpreter` is not thread-safe**, so every call is serialised
   on a lock. Two rapid classifications cannot race into native code.
3. **Model loading is lazy and off the main thread.** Nothing in the UI thread
   ever touches disk, Room, or the interpreter.

---

## 3:15 — Works without permission (30s)

Deny location permission.

> "With no GPS fix the app stays completely usable. It falls back to clearly
> labelled sample coordinates and tells you so, rather than showing an empty
> screen."

> [!TIP]
> Good moment to mention: `INTERNET` is not in the manifest. The offline
> claim is verifiable, not aspirational.

---

## 3:45 — Close (15s)

> "Clean Architecture in one module — `domain` is pure Kotlin with no Android
> or Room imports, `data` implements the domain's interfaces, and the UI is
> stateless Composables over a single immutable `StateFlow`. 48 unit tests,
> zero lint errors. The main limitation is that the bundled model is a small
> embedding MLP rather than the transformer the original docs claimed — the
> README documents the exact steps to build a real MobileBERT version."

---

## Anticipated questions

**"Is that really running on device?"**
Yes — `tools/probe_semantics.py` reproduces the same predictions in Python,
and the app's banner prints the model's introspected tensor signature.

**"Why not Hilt?"**
The graph is small and fully constructor-injected. `AppContainer` is ~50 lines
and makes the dependency list for the screen visible in one place.

**"How do you test it?"**
`FeedViewModelTest` covers state transitions, successful and failed publishing,
duplicate-submit prevention, and stale-classification rejection — using virtual
time, so the debounce assertions are deterministic rather than sleep-based.

**"What's the biggest thing you'd change next?"**
Add a real instrumented test asserting the live model's contract, and replace
the hardcoded landmark table with a proper offline reverse-geocoding dataset.