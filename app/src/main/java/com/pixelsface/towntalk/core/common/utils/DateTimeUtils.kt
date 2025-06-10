package com.pixelsface.towntalk.core.common.utils

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    private const val MINUTE_MILLIS = 60 * 1000L
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun formatLastSeenTimestamp(timestamp: Long?, isOnline: Boolean, context: Context? = null): String {
        if (isOnline) return "Online"
        if (timestamp == null || timestamp <= 0) return "Offline"

        val now = System.currentTimeMillis()
        if (timestamp > now || timestamp <= 0) { // Future or invalid timestamps
            return "Offline"
        }

        val diff = now - timestamp

        return when {
            diff < MINUTE_MILLIS -> "Last seen just now"
            diff < HOUR_MILLIS -> "Last seen ${diff / MINUTE_MILLIS}m ago"
            diff < DAY_MILLIS -> {
                // Today
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                "Last seen today at ${sdf.format(Date(timestamp))}"
            }
            diff < 2 * DAY_MILLIS -> {
                // Yesterday
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                "Last seen yesterday at ${sdf.format(Date(timestamp))}"
            }
            diff < 7 * DAY_MILLIS -> {
                // Within the last week
                val sdf = SimpleDateFormat("E hh:mm a", Locale.getDefault()) // E.g., "Mon hh:mm a"
                "Last seen ${sdf.format(Date(timestamp))}"
            }
            else -> {
                // Older than a week
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                "Last seen on ${sdf.format(Date(timestamp))}"
            }
        }
    }

    // Simple time formatter (hh:mm a) - already in ChatScreen, can be centralized here too
    fun formatTime(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    // Contextual relative time span string, useful for general purpose relative times.
    // For "last seen", the above function is more specific.
    fun getRelativeTimeSpan(timestamp: Long, context: Context?): String {
        if (context == null) { // Fallback if no context
            return formatLastSeenTimestamp(timestamp, false, null) // Basic fallback
        }
        val now = System.currentTimeMillis()
        //val difference = now - timestamp // Not directly used by DateUtils.getRelativeTimeSpanString in this form
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }
} 