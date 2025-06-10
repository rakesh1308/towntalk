package com.pixelsface.towntalk.features.events.navigation

sealed class EventScreenRoute(val route: String) {
    object Events : EventScreenRoute("events_list/{city}") { // Takes city as argument
        fun createRoute(city: String) = "events_list/$city"
    }
    object EventDetail : EventScreenRoute("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object CreateEvent : EventScreenRoute("create_event")
} 