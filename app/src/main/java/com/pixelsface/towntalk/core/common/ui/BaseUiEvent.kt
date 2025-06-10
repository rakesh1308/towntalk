package com.pixelsface.towntalk.core.common.ui

import com.pixelsface.towntalk.core.common.error.TownTalkException

/**
 * Sealed class representing common UI events that can be emitted by ViewModels
 * and handled by the UI layer.
 */
sealed class BaseUiEvent {
    /**
     * Event emitted when an error occurs.
     */
    data class Error(val exception: TownTalkException) : BaseUiEvent()

    /**
     * Event emitted when a message should be shown to the user.
     */
    data class ShowMessage(val message: String) : BaseUiEvent()

    /**
     * Event emitted when navigation should occur.
     */
    data class Navigate(val route: String) : BaseUiEvent()
} 