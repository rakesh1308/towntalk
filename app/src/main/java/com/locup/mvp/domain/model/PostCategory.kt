package com.locup.mvp.domain.model

/**
 * The five labels the on-device classifier predicts.
 *
 * The wire format in `assets/labels.txt` is lowercase and alphabetical
 * (`civic, emergency, event, general, traffic`) — that order defines the
 * model's output indices and must not be changed casually. [fromAssetLabel]
 * maps those raw strings onto this enum so the rest of the app deals in
 * typed values instead of magic strings.
 */
enum class PostCategory {
    Emergency,
    Traffic,
    Event,
    Civic,
    General;

    companion object {
        /**
         * Case-insensitive parse of a raw label string.
         *
         * Returns null (rather than throwing) for unknown input so a changed
         * or corrupt `labels.txt` degrades to [General] at the call site
         * instead of crashing the classifier mid-keystroke.
         */
        fun fromAssetLabel(raw: String): PostCategory? =
            entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }

        /** Same as [fromAssetLabel] but never null. */
        fun fromAssetLabelOrGeneral(raw: String): PostCategory =
            fromAssetLabel(raw) ?: General
    }
}