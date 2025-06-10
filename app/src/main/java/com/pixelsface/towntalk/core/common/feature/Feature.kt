package com.pixelsface.towntalk.core.common.feature

enum class Feature {
    // Authentication Features
    PHONE_AUTH,
    GOOGLE_AUTH,
    EMAIL_AUTH,
    
    // Feed Features
    POST_CREATION,
    POST_LIKES,
    POST_COMMENTS,
    POST_MEDIA,
    
    // Profile Features
    PROFILE_EDITING,
    PROFILE_PRIVACY,
    
    // Location Features
    LOCATION_BASED_FEED,
    LOCATION_SHARING,
    
    // UI Features
    NEW_UI,
    DARK_MODE,
    
    // Backend Features
    SPRING_BOOT_BACKEND,
    OFFLINE_SUPPORT,
    
    // Social Features
    PRIVATE_MESSAGING,
    EVENT_RSVP;
    
    val defaultValue: Boolean
        get() = when (this) {
            // Authentication features are enabled by default
            PHONE_AUTH, GOOGLE_AUTH, EMAIL_AUTH -> true
            
            // Core features are enabled by default
            POST_CREATION, POST_LIKES, POST_COMMENTS, POST_MEDIA -> true
            PROFILE_EDITING, PROFILE_PRIVACY -> true
            LOCATION_BASED_FEED -> true
            
            // Experimental features are disabled by default
            LOCATION_SHARING, NEW_UI, DARK_MODE -> false
            SPRING_BOOT_BACKEND, OFFLINE_SUPPORT -> false
            PRIVATE_MESSAGING, EVENT_RSVP -> false
        }
} 