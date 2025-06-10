# TownTalk Search Screens

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

### Components
1. **Search Bar**
   - Outlined text field with search icon
   - Voice search button
   - Clear text button
   - Height: 56dp
   - Corner radius: 28dp
   - Elevation: 2dp

2. **Filter Chips**
   - Horizontal scrollable list
   - Selected/unselected states
   - Icon + label design
   - Height: 32dp
   - Corner radius: 16dp

3. **Search Results**
   - List or grid layout
   - Card-based design
   - Preview images
   - Relevance indicators
   - Item height: variable

4. **Recent Searches**
   - List layout with icons
   - Clear button per item
   - Clear all option
   - Item height: 48dp

## Main Search Screen

```
┌─────────────────────────────────────────┐
│ Search TownTalk                         │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🔍 Search posts, events, users...   │ │
│ │                               🎤    │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Filters                                │
│ ┌───┐ ┌───┐ ┌───┐ ┌───┐ ┌───┐         │
│ │All│ │📝 │ │📅 │ │👤 │ │📍 │         │
│ └───┘ └───┘ └───┘ └───┘ └───┘         │
│                                        │
│ Trending Topics                        │
│ ┌─────────────────────────────────────┐ │
│ │ 🔥 Community Clean-up               │ │
│ │ 1.2K posts                          │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ 🔥 Local Festival                   │ │
│ │ 856 posts                           │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Recent Searches                        │
│ ┌─────────────────────────────────────┐ │
│ │ 🕒 "park events"              ✕    │ │
│ ├─────────────────────────────────────┤ │
│ │ 🕒 "food trucks"              ✕    │ │
│ ├─────────────────────────────────────┤ │
│ │ 🕒 "community garden"         ✕    │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Popular Near You                       │
│ ┌─────────────────────────────────────┐ │
│ │ 📍 Central Park                     │ │
│ │ 15 active discussions               │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Search Results Screen

```
┌─────────────────────────────────────────┐
│ ← Back                                 │
├─────────────────────────────────────────┤
│ ┌─────────────────────────────────────┐ │
│ │ 🔍 community events        ✕   🎤   │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Filters                                │
│ ┌───┐ ┌───┐ ┌───┐ ┌───┐               │
│ │All│ │New│ │Top│ │📍 │               │
│ └───┘ └───┘ └───┘ └───┘               │
│                                        │
│ 24 Results                             │
│                                        │
│ ┌─────────────────────────────────────┐ │
│ │ 📝 Community Fair 2024              │ │
│ │ Join us for the annual...           │ │
│ │ [Image Preview]                     │ │
│ │ 🗓️ March 15  •  ❤️ 45  •  💬 12    │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ ┌─────────────────────────────────────┐ │
│ │ 👤 Community Events Group           │ │
│ │ 1.2K members  •  Active             │ │
│ │ [Group Preview]                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ ┌─────────────────────────────────────┐ │
│ │ 📅 Upcoming Events                  │ │
│ │ Next 7 days                         │ │
│ │ [Calendar Preview]                  │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Advanced Search Screen

```
┌─────────────────────────────────────────┐
│ ← Back                      Reset All  │
├─────────────────────────────────────────┤
│ Advanced Search                         │
│                                        │
│ Keywords                               │
│ ┌─────────────────────────────────────┐ │
│ │ Enter keywords...                   │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Category                               │
│ ┌─────────────────────────────────────┐ │
│ │ ○ All Categories                    │ │
│ │ ● Events                           │ │
│ │ ○ Posts                            │ │
│ │ ○ Users                            │ │
│ │ ○ Groups                           │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Date Range                             │
│ ┌─────────────────────────────────────┐ │
│ │ From: [Date Picker]                 │ │
│ │ To: [Date Picker]                   │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Location                               │
│ ┌─────────────────────────────────────┐ │
│ │ Current Location                    │ │
│ │ Distance: 5 miles [Slider]          │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Sort By                                │
│ ┌─────────────────────────────────────┐ │
│ │ ○ Relevance                         │ │
│ │ ● Date (Newest)                    │ │
│ │ ○ Popularity                       │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Apply Filters]                        │
└─────────────────────────────────────────┘
```

## Voice Search Screen

```
┌─────────────────────────────────────────┐
│ ← Back                                 │
├─────────────────────────────────────────┤
│                                        │
│                                        │
│           [Microphone Icon]            │
│                                        │
│         Listening for search...        │
│                                        │
│         "Show me events near..."       │
│                                        │
│           [Cancel Button]              │
│                                        │
└─────────────────────────────────────────┘
```

## Loading State

```
┌─────────────────────────────────────────┐
│ ┌─────────────────────────────────────┐ │
│ │ 🔍 [Shimmer Effect]                 │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Shimmer Effect - Filters]             │
│                                        │
│ [Shimmer Effect - Result Card]         │
│                                        │
│ [Shimmer Effect - Result Card]         │
│                                        │
│ [Shimmer Effect - Result Card]         │
│                                        │
└─────────────────────────────────────────┘
```

## Error State

```
┌─────────────────────────────────────────┐
│                                        │
│           [Error Icon]                 │
│                                        │
│     Unable to perform search           │
│                                        │
│     Please check your connection       │
│     and try again                      │
│                                        │
│           [Retry Button]               │
│                                        │
└─────────────────────────────────────────┘
```

## Interactions

### Search Actions
- Real-time search suggestions
- Voice search activation
- Filter selection and deselection
- Clear search history
- Save search preferences

### Navigation
- Smooth transitions between results
- Filter panel animations
- Voice search modal
- Back navigation preservation
- Deep linking support

### Animations
- Search bar focus/unfocus
- Filter chip selection
- Voice input visualization
- Results loading transitions
- Error state transitions

## Accessibility

### Screen Reader Support
- Search field descriptions
- Filter state announcements
- Result summaries
- Voice search instructions
- Error notifications

### Visual Accessibility
- High contrast mode
- Adjustable text size
- Color blind friendly
- Focus indicators
- Touch target size

### Interaction Accessibility
- Voice search alternative
- Keyboard navigation
- Reduced motion
- Alternative text
- Haptic feedback

## Analytics Integration

### Search Metrics
- Search query frequency
- Filter usage patterns
- Voice search adoption
- Result click-through
- Search abandonment

### Performance Metrics
- Search response time
- Suggestion latency
- Filter application speed
- Voice recognition accuracy
- Error frequency

### User Behavior
- Popular search terms
- Common filter combinations
- Search refinement patterns
- Result interaction depth
- Session duration

## Design Notes

### Colors
- Primary: #1976D2 (Blue)
- Secondary: #4CAF50 (Green)
- Background: #F5F5F5 (Light Gray)
- Surface: #FFFFFF (White)
- Text: #000000 (Black)
- Text Secondary: #757575 (Gray)
- Error: #D32F2F (Red)
- Success: #4CAF50 (Green)

### Typography
- Search Input: Roboto Regular, 16sp
- Filter Labels: Roboto Medium, 14sp
- Result Title: Roboto Medium, 16sp
- Result Description: Roboto Regular, 14sp
- Section Headers: Roboto Medium, 20sp
- Metadata: Roboto Regular, 12sp

### Components
1. **Search Bar**
   - Height: 56dp
   - Corner radius: 28dp
   - Icon size: 24dp
   - Text padding: 16dp

2. **Filter Chips**
   - Height: 32dp
   - Corner radius: 16dp
   - Icon size: 18dp
   - Horizontal spacing: 8dp

3. **Result Cards**
   - Corner radius: 8dp
   - Elevation: 1dp
   - Image aspect ratio: 16:9
   - Padding: 16dp

4. **Voice Search**
   - Microphone size: 64dp
   - Animation duration: 300ms
   - Ripple effect
   - Sound visualization 