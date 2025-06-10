# TownTalk Feed Screens

## Design System

### Colors
```kotlin
object TownTalkColors {
    // Light Theme
    val Primary = Color(0xFF1976D2)
    val PrimaryDark = Color(0xFF1565C0)
    val PrimaryLight = Color(0xFF42A5F5)
    val Secondary = Color(0xFF4CAF50)
    val SecondaryDark = Color(0xFF388E3C)
    val SecondaryLight = Color(0xFF81C784)
    val Background = Color(0xFFF5F5F5)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFD32F2F)
    
    // Dark Theme
    val DarkPrimary = Color(0xFF90CAF9)
    val DarkPrimaryDark = Color(0xFF64B5F6)
    val DarkPrimaryLight = Color(0xFFBBDEFB)
    val DarkSecondary = Color(0xFF81C784)
    val DarkSecondaryDark = Color(0xFF66BB6A)
    val DarkSecondaryLight = Color(0xFFA5D6A7)
    val DarkBackground = Color(0xFF121212)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkError = Color(0xFFEF5350)
}
```

### Typography
```kotlin
val TownTalkTypography = Typography(
    h1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    h2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 0.sp
    ),
    h3 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.15.sp
    ),
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 1.25.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    )
)
```

### Spacing
```kotlin
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

### Components
1. **Top App Bar**
   - Two-line title with app name and location
   - Profile avatar on the left
   - Search and notifications icons on the right
   - Elevation: 2dp
   - Height: 64dp (collapsed) / 128dp (expanded)

2. **Post Card**
   - User avatar and name
   - Timestamp and category
   - Title and content
   - Image gallery with multiple image support
   - Action buttons (like, comment, share, save)
   - Comments section with "View all" option
   - Elevation: 1dp
   - Corner radius: 8dp
   - Padding: 16dp

3. **Bottom Navigation**
   - Home, Explore, Events, and Profile tabs
   - Active tab highlighted
   - Icons with labels
   - Height: 56dp
   - Elevation: 8dp

4. **Floating Action Button**
   - Circular button with plus icon
   - Primary color
   - Positioned above bottom navigation
   - Size: 56dp
   - Elevation: 6dp

## Main Feed Screen

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
│ New York                                │
├─────────────────────────────────────────┤
│ ┌─────┐ John Doe                        │
│ │     │ 2h ago • General               │
│ └─────┘                                │
│                                        │
│ Local Event: Summer Music Festival     │
│                                        │
│ Join us this weekend for the annual    │
│ summer music festival! Live bands,     │
│ food trucks, and more...               │
│                                        │
│ [Image Gallery]                        │
│ ┌─────┐ ┌─────┐ ┌─────┐               │
│ │     │ │     │ │     │               │
│ └─────┘ └─────┘ └─────┘               │
│                                        │
│ ❤️ 24  💬 8  🔄 Share  🔖 Save        │
│                                        │
│ View all 8 comments                    │
├─────────────────────────────────────────┤
│ ┌─────┐ Jane Smith                     │
│ │     │ 5h ago • News                 │
│ └─────┘                                │
│                                        │
│ Breaking: New Park Opening             │
│                                        │
│ The city's new recreational park will  │
│ open next month. Features include...   │
│                                        │
│ [Single Image]                         │
│                                        │
│ ❤️ 45  💬 12  🔄 Share  🔖 Save       │
│                                        │
│ View all 12 comments                   │
├─────────────────────────────────────────┤
│                                        │
│     [Floating Action Button +]         │
│                                        │
├─────────────────────────────────────────┤
│ 🏠 Home  🔍 Explore  📅 Events  👤 Profile │
└─────────────────────────────────────────┘
```

## Loading State

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
│ New York                                │
├─────────────────────────────────────────┤
│ ┌─────┐ [Shimmer]                       │
│ │     │ [Shimmer]                       │
│ └─────┘                                │
│                                        │
│ [Shimmer]                              │
│ [Shimmer]                              │
│ [Shimmer]                              │
│                                        │
│ [Shimmer Box]                          │
│                                        │
│ [Shimmer] [Shimmer] [Shimmer]          │
│                                        │
│ [Shimmer] [Shimmer] [Shimmer] [Shimmer]│
├─────────────────────────────────────────┤
│ ┌─────┐ [Shimmer]                       │
│ │     │ [Shimmer]                       │
│ └─────┘                                │
│                                        │
│ [Shimmer]                              │
│ [Shimmer]                              │
│ [Shimmer]                              │
│                                        │
│ [Shimmer Box]                          │
└─────────────────────────────────────────┘
```

## Error State

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
│ New York                                │
├─────────────────────────────────────────┤
│                                        │
│     [Error Icon]                        │
│                                        │
│     Unable to load posts                │
│                                        │
│     Please check your internet          │
│     connection and try again            │
│                                        │
│     [Retry Button]                      │
│                                        │
└─────────────────────────────────────────┘
```

## Interactions

### Pull to Refresh
- Pull down gesture to refresh feed
- Loading indicator with primary color
- Haptic feedback on refresh
- Smooth animation for content update

### Image Gallery
- Swipe to view multiple images
- Tap to view full screen
- Pinch to zoom
- Double tap to like
- Image count indicator
- Loading placeholder
- Error state with retry option

### Comments
- Tap to expand/collapse
- Swipe to reveal actions
- Like and reply options
- Haptic feedback on interactions
- Smooth animations for transitions

### Navigation
- Smooth transitions between screens
- Bottom sheet for location selection
- Back gesture support
- Haptic feedback on navigation
- Loading states during transitions

## Accessibility

### Screen Reader Support
- Content descriptions for all images
- Action descriptions for buttons
- Proper heading hierarchy
- Clear navigation announcements

### Visual Accessibility
- High contrast mode support
- Adjustable text size
- Color blind friendly palette
- Clear focus indicators

### Interaction Accessibility
- Large touch targets (minimum 48dp)
- Keyboard navigation support
- Voice control support
- Reduced motion option

## Performance Considerations

### Image Loading
- Progressive image loading
- Image caching
- Placeholder images
- Error handling
- Retry mechanism

### List Performance
- Lazy loading
- View recycling
- Pagination
- Pull to refresh
- Infinite scroll

### Animation Performance
- Hardware acceleration
- Frame rate monitoring
- Memory usage optimization
- Battery usage consideration

## Analytics Integration

### User Engagement
- Post view time
- Scroll depth
- Interaction rate
- Share rate
- Comment rate

### Performance Metrics
- Load time
- Frame rate
- Memory usage
- Battery impact
- Network usage

### Error Tracking
- Crash reports
- Error logs
- User feedback
- Performance issues
- Network errors

## Create Post Screen

```
┌─────────────────────────────────────────┐
│ ← Back                    Post         │
├─────────────────────────────────────────┤
│ ┌─────┐ John Doe                        │
│ │     │                                │
│ └─────┘                                │
│                                        │
│ Title                                   │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Content                                │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ │                                     │ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Add Photos/Videos]  📍 Location       │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Category                               │
│ ┌─────────────────────────────────────┐ │
│ │ General ▼                          │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Post Button]                          │
└─────────────────────────────────────────┘
```

## Post Detail Screen

```
┌─────────────────────────────────────────┐
│ ← Back                    ⋮ More       │
├─────────────────────────────────────────┤
│ ┌─────┐ John Doe                        │
│ │     │ 2h ago • General               │
│ └─────┘                                │
│                                        │
│ Local Event: Summer Music Festival     │
│                                        │
│ Join us this weekend for the annual    │
│ summer music festival! Live bands,     │
│ food trucks, and more...               │
│                                        │
│ [Image Gallery]                        │
│ ┌─────┐ ┌─────┐ ┌─────┐               │
│ │     │ │     │ │     │               │
│ └─────┘ └─────┘ └─────┘               │
│                                        │
│ ❤️ 24  💬 8  🔄 Share  🔖 Save        │
│                                        │
│ Comments                               │
│ ┌─────────────────────────────────────┐ │
│ │ ┌───┐ Jane: Great event!           │ │
│ │ │   │ 1h ago                      │ │
│ │ └───┘                             │ │
│ │                                   │ │
│ │ ┌───┐ Mike: Can't wait!           │ │
│ │ │   │ 30m ago                     │ │
│ │ └───┘                             │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Write a comment...                     │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Location Selection Screen

```
┌─────────────────────────────────────────┐
│ ← Back                    Done         │
├─────────────────────────────────────────┤
│ 🔍 Search location...                   │
├─────────────────────────────────────────┤
│ 📍 Current Location                     │
│ New York, NY                            │
│                                        │
│ Popular Cities                          │
│ ┌─────────────────────────────────────┐ │
│ │ Los Angeles, CA                     │ │
│ ├─────────────────────────────────────┤ │
│ │ Chicago, IL                         │ │
│ ├─────────────────────────────────────┤ │
│ │ Houston, TX                         │ │
│ ├─────────────────────────────────────┤ │
│ │ Phoenix, AZ                         │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Recent Locations                        │
│ ┌─────────────────────────────────────┐ │
│ │ Boston, MA                          │ │
│ ├─────────────────────────────────────┤ │
│ │ Seattle, WA                         │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Design Notes

### Colors
- Primary: #1976D2 (Blue)
- Secondary: #4CAF50 (Green)
- Background: #F5F5F5 (Light Gray)
- Surface: #FFFFFF (White)
- Text: #000000 (Black)
- Text Secondary: #757575 (Gray)

### Typography
- Headings: Roboto Bold, 20sp
- Body: Roboto Regular, 16sp
- Caption: Roboto Regular, 14sp
- Button: Roboto Medium, 16sp

### Components
1. **Top App Bar**
   - Two-line title with app name and location
   - Profile avatar on the left
   - Search and notifications icons on the right

2. **Post Card**
   - User avatar and name
   - Timestamp and category
   - Title and content
   - Image gallery with multiple image support
   - Action buttons (like, comment, share, save)
   - Comments section with "View all" option

3. **Bottom Navigation**
   - Home, Explore, Events, and Profile tabs
   - Active tab highlighted
   - Icons with labels

4. **Floating Action Button**
   - Circular button with plus icon
   - Primary color
   - Positioned above bottom navigation
   - Size: 56dp
   - Elevation: 6dp

5. **Create Post Screen**
   - Back button and Post action
   - Title and content fields
   - Image upload area
   - Location picker
   - Category selector
   - Post button

6. **Post Detail Screen**
   - Full post content
   - Image gallery
   - Comments section
   - Comment input field

7. **Location Selection**
   - Search bar
   - Current location
   - Popular cities list
   - Recent locations
   - Done button

### Interactions
1. **Pull to Refresh**
   - Pull down to refresh feed
   - Loading indicator with primary color

2. **Image Gallery**
   - Swipe to view multiple images
   - Tap to view full screen
   - Image count indicator

3. **Comments**
   - Tap to expand/collapse
   - Swipe to reveal actions
   - Like and reply options

4. **Navigation**
   - Smooth transitions between screens
   - Bottom sheet for location selection
   - Back gesture support 