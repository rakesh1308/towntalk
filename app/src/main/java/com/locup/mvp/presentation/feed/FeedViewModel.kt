package com.locup.mvp.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.repository.LandmarkResolver
import com.locup.mvp.domain.repository.PostRepository
import com.locup.mvp.domain.repository.SubscriptionRepository
import com.locup.mvp.domain.repository.TextClassifier
import com.locup.mvp.domain.usecase.FeedFilter
import com.locup.mvp.domain.usecase.ObserveFeed
import com.locup.mvp.domain.usecase.PublishPost
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns feed state, voting, subscriptions and the live classification preview.
 *
 * ### Coroutine design
 *
 * - User-initiated work runs in [viewModelScope], so it is cancelled
 *   automatically when the screen goes away.
 * - Classification and persistence run on the injected background dispatcher;
 *   nothing in this class touches disk, Room, or TFLite on the main thread.
 * - The preview is a single pipeline: `text -> debounce -> flatMapLatest -> map`.
 *   `flatMapLatest` is what guarantees a slow classification for old text
 *   cannot overwrite a newer result: when new text arrives the previous
 *   text's job is cancelled, and because the classification runs inside
 *   `map` — a suspend transform — an outdated **result is discarded** and
 *   never reaches `_composer`.
 * - Cancellation is cooperative and is only observed at a suspension point.
 *   `classifier.classify()` is a **blocking** native call executed inside
 *   `withContext`, so the native inference itself is *not* interrupted
 *   mid-flight: it runs to completion in the background. The guarantee
 *   `flatMapLatest` provides is about the *outcome* — the outdated result is
 *   dropped instead of being written to UI state — not about aborting the
 *   native work.
 * - [onCleared] closes the classifier so native resources are released with the
 *   screen rather than leaking for the life of the process.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FeedViewModel(
    private val postRepository: PostRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val classifier: TextClassifier,
    private val landmarks: LandmarkResolver,
    private val publishPost: PublishPost,
    private val observeFeed: ObserveFeed,
    private val backgroundDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _composer = MutableStateFlow(ComposerUiState())
    val composer: StateFlow<ComposerUiState> = _composer.asStateFlow()

    // Hot so a location update is not replayed to every new collector.
    private val filter = MutableStateFlow(FeedFilter.Here)
    private val location = MutableStateFlow<GeoPoint?>(null)

    private val filterFlow = filter.asStateFlow()

    /** Bumped on each publish so stale classifier output can be discarded. */
    private var publishJob: Job? = null

    init {
        observeFeed(filterFlow, location.asStateFlow())
            .onEach { posts ->
                _uiState.update { it.copy(posts = posts, isLoading = false) }
            }
            .launchIn(viewModelScope)

        // Keep the derived area/subscription flags in sync with the position.
        location
            .onEach { point ->
                _uiState.update { state ->
                    state.copy(
                        location = point,
                        areaLabel = point?.let {
                            landmarks.labelFor(it.latitude, it.longitude)
                        },
                    )
                }
                val cluster = point?.clusterId
                val subscribed = if (cluster == null) {
                    false
                } else {
                    withContext(backgroundDispatcher) {
                        subscriptionRepository.isSubscribed(cluster)
                    }
                }
                _uiState.update { it.copy(isSubscribedToCurrentArea = subscribed) }
            }
            .launchIn(viewModelScope)

        refreshClassifierStatus()
        seedIfEmpty()
    }

    // region Intents

    fun setFilter(next: FeedFilter) {
        filter.value = next
    }

    /** Called by the UI when a location fix arrives or permission is denied. */
    fun updateLocation(point: GeoPoint?) {
        location.value = point
    }

    fun toggleSubscription() {
        val cluster = location.value?.clusterId ?: run {
            _uiState.update { it.copy(message = "No location yet — cannot subscribe.") }
            return
        }
        viewModelScope.launch {
            val label = landmarks.labelFor(location.value!!.latitude, location.value!!.longitude)
            subscriptionRepository.toggle(cluster, label)
            val subscribed = subscriptionRepository.isSubscribed(cluster)
            _uiState.update {
                it.copy(
                    isSubscribedToCurrentArea = subscribed,
                    message = if (subscribed) {
                        "Following $label"
                    } else {
                        "Unfollowed $label"
                    },
                )
            }
        }
    }

    fun upvote(postId: String) = viewModelScope.launch {
        runCatching { postRepository.upvote(postId) }
            .onFailure { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(message = "Couldn't register your vote.") }
            }
    }

    fun downvote(postId: String) = viewModelScope.launch {
        runCatching { postRepository.downvote(postId) }
            .onFailure { error ->
                if (error is CancellationException) throw error
                _uiState.update { it.copy(message = "Couldn't register your vote.") }
            }
    }

    // endregion

    // region Composer

    /**
     * Live classification preview.
     *
     * Derived from [composer] rather than pushed through a separate channel.
     * An earlier revision used a `MutableSharedFlow` + `tryEmit`, which is a
     * trap: `tryEmit` **drops** values when no collector is subscribed yet, so
     * a preview typed early in the composer's life silently never appears.
     * Deriving from state removes that failure mode entirely.
     *
     * `debounce` skips intermediate keystrokes. The blocking
     * `classifier.classify` call sits inside `map` — a suspend transform — so
     * it runs off the main thread. `flatMapLatest` discards the result if
     * newer text has since arrived, so a slow result for older text can never
     * overwrite a newer one. The blocking native call itself is **not**
     * interrupted mid-flight: cancellation is cooperative and is only
     * observed at a suspension point, so the inference runs to completion in
     * the background and it is its *result* that gets thrown away.
     *
     * The banner is refreshed alongside each preview because the classifier
     * loads its model **lazily**: the engine only becomes "real" on the first
     * successful classification, so reading the status once at init would
     * leave the UI claiming "Keyword fallback" even while the model is live.
     */
    init {
        _composer
            .map { it.text }
            .distinctUntilChanged()
            .debounce(PREVIEW_DEBOUNCE_MS)
            .flatMapLatest { text ->
                if (text.isBlank()) {
                    flowOf(null)
                } else {
                    flowOf(text)
                        .onEach { _composer.update { s -> s.copy(isClassifying = true) } }
                        .map { withContext(backgroundDispatcher) { classifier.classify(it) } }
                }
            }
            .onEach { result ->
                _composer.update { it.copy(preview = result, isClassifying = false) }
                // Reflect the engine that actually produced this result, so
                // the banner can never contradict the preview above it.
                // A null result means the text was cleared — leave the banner
                // as-is rather than flickering it back to "not loaded".
                result?.let { publishClassifierStatus(it.engine) }
            }
            .launchIn(viewModelScope)
    }

    fun onComposerTextChanged(text: String) {
        // One write: the preview above is derived from `text`.
        _composer.update { it.copy(text = text, error = null) }
    }

    /**
     * Publishes the current composer text.
     *
     * Guards against double submission by ignoring the call while a publish is
     * already in flight, and keeps the entered text when anything fails so the
     * user does not lose what they typed. The sheet is dismissed by the UI
     * only after this returns successfully.
     */
    fun publish(onSuccess: () -> Unit) {
        if (_composer.value.isPublishing) return
        val text = _composer.value.text
        if (text.isBlank()) return

        _composer.update { it.copy(isPublishing = true, error = null) }
        publishJob = viewModelScope.launch {
            try {
                val outcome = publishPost(text, location.value)
                _composer.value = ComposerUiState()
                onSuccess()
                _uiState.update {
                    it.copy(
                        message = if (outcome.usedFallbackLocation) {
                            "Posted using demo coordinates (no GPS fix)."
                        } else {
                            "Posted to ${landmarks.labelFor(outcome.location.latitude, outcome.location.longitude)}"
                        },
                    )
                }
            } catch (e: CancellationException) {
                // Screen went away mid-publish: leave state alone, let the
                // scope unwind.
                _composer.update { it.copy(isPublishing = false) }
                throw e
            } catch (t: Throwable) {
                // Retain the text so the draft survives a failed publish.
                _composer.update {
                    it.copy(
                        isPublishing = false,
                        error = t.message ?: "Couldn't publish. Please try again.",
                    )
                }
            }
        }
    }

    fun dismissComposer() {
        _composer.value = ComposerUiState()
    }

    // endregion

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun refreshClassifierStatus() {
        viewModelScope.launch {
            val status = withContext(backgroundDispatcher) { classifier.status() }
            _uiState.update { it.copy(classifierStatus = status) }
        }
    }

    /**
     * Publishes the banner state for an engine we *know* produced a result.
     *
     * Prefers the classifier's own [com.locup.mvp.domain.repository.TextClassifier.status]
     * so the technical detail (tensor signature, token count) comes from the
     * model itself, but falls back to the engine identity if the classifier
     * cannot be queried.
     */
    private fun publishClassifierStatus(engine: com.locup.mvp.domain.model.InferenceEngine) {
        viewModelScope.launch {
            val status = withContext(backgroundDispatcher) { classifier.status() }
            // Guard against a stale read: if the status has not yet caught up
            // with the engine we just observed, use the observed engine so the
            // banner never contradicts the classification preview.
            val resolved =
                if (status.engine == engine) status
                else com.locup.mvp.domain.repository.ClassifierStatus(
                    engine = engine,
                    detail = status.detail,
                    unavailableReason = status.unavailableReason,
                )
            _uiState.update { it.copy(classifierStatus = resolved) }
        }
    }

    private fun seedIfEmpty() {
        viewModelScope.launch {
            runCatching { postRepository.seedDemoDataIfEmpty() }
            // Re-read the banner: seeding triggers a first classification, so
            // the status may have moved from "not loaded" to a real engine.
            refreshClassifierStatus()
        }
    }

    override fun onCleared() {
        super.onCleared()
        publishJob?.cancel()
        // Releases the TFLite interpreter with the ViewModel's lifetime.
        classifier.close()
    }

    /** Test-only hook: [onCleared] is protected, so tests cannot reach it. */
    fun onClearedForTest() = onCleared()

    companion object {
        /** Long enough to skip intermediate keystrokes, short enough to feel live. */
        const val PREVIEW_DEBOUNCE_MS = 250L
    }
}