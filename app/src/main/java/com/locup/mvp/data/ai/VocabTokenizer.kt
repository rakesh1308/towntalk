package com.locup.mvp.data.ai

/**
 * Tokenizer for the bundled word-embedding classifier.
 *
 * ## Why this exists
 *
 * The project previously shipped a hand-rolled BERT WordPiece tokenizer
 * (`BertTokenizer`) built for a MobileBERT model. That model was **never
 * actually bundled**. Inspecting the real `.tflite` with `ai-edge-litert`
 * shows the true contract:
 *
 * ```
 * input : serving_default_keras_tensor:0  int32  [1, 24]
 * output: StatefulPartitionedCall_1:0     float32[1, 5]
 * ops   : Embedding(GatherV2) -> Dense(ReLU) -> Dense(BiasAdd)
 * ```
 *
 * A single int32 tensor of 24 token ids. So this tokenizer implements the
 * matching scheme — verified end-to-end to classify all five categories
 * correctly (see `tools/probe_semantics.py` and the README table):
 *
 *  1. lowercase;
 *  2. strip everything that is not a letter or digit;
 *  3. map each token through `vocab.json`, using `<OOV>` for unknown words;
 *  4. truncate to [sequenceLength], then right-pad with `<PAD>`.
 *
 * There is no `[CLS]`/`[SEP]` pair: the model has no segment embeddings, and
 * its single pooled vector comes straight from the embedding bag.
 *
 * @param vocab token string -> id, exactly as stored in `assets/vocab.json`.
 * @param sequenceLength the model's fixed input width (24).
 */
class VocabTokenizer(
    private val vocab: Map<String, Int>,
    private val sequenceLength: Int,
) {
    // Both specials are required; a vocab missing them cannot encode at all,
    // so fail loudly at construction rather than silently producing garbage.
    private val padId: Int = requireNotNull(vocab[PAD]) { "vocab.json is missing $PAD" }
    private val oovId: Int = requireNotNull(vocab[OOV]) { "vocab.json is missing $OOV" }

    /**
     * Encode [text] into a fixed-width id array.
     *
     * Always returns exactly [sequenceLength] elements so the tensor shape
     * matches the model and no runtime re-shaping is needed.
     */
    fun encode(text: String): IntArray {
        val ids = IntArray(sequenceLength) { padId }
        var index = 0
        for (token in tokenize(text)) {
            if (index >= sequenceLength) break
            ids[index++] = vocab[token] ?: oovId
        }
        return ids
    }

    /**
     * Splits on non-alphanumeric characters after lowercasing.
     *
     * Kept public and side-effect free so the tokenisation rules can be unit
     * tested on the JVM without loading the model.
     */
    fun tokenize(text: String): List<String> =
        text.lowercase()
            .split(NON_ALPHANUMERIC)
            .filter { it.isNotEmpty() }

    companion object {
        const val PAD = "<PAD>"
        const val OOV = "<OOV>"

        private val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")
    }
}