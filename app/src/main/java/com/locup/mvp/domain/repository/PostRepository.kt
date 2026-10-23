package com.locup.mvp.domain.repository

import com.locup.mvp.domain.model.GeoPoint
import com.locup.mvp.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Persistence port for posts.
 *
 * Declared in `domain` and implemented in `data`, so the direction of
 * dependency points inwards: the UI and use cases depend on this interface,
 * never on Room.
 */
interface PostRepository {

    /** All posts, newest first. Emits again whenever the table changes. */
    fun observePosts(): Flow<List<Post>>

    /**
     * Classify then persist a post.
     *
     * Classification happens here (not in the ViewModel) because the label
     * and confidence are persisted properties of the post; re-classifying on
     * every read would make stored rows mutable in meaning.
     */
    suspend fun publish(text: String, location: GeoPoint, author: String = DEFAULT_AUTHOR)

    suspend fun upvote(postId: String)

    suspend fun downvote(postId: String)

    /** Populates clearly-labelled demo rows when the table is empty. */
    suspend fun seedDemoDataIfEmpty()

    companion object {
        const val DEFAULT_AUTHOR = "You"
    }
}