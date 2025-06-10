# TownTalk Profile Screens

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

### Components
1. **Profile Header**
   - Large circular avatar
   - Edit profile button
   - Profile completion indicator
   - User stats (posts, likes, comments)
   - Location and join date
   - Height: 200dp

2. **Achievement Badges**
   - Horizontal scrollable list
   - Icon + label design
   - Progress indicators
   - Height: 100dp

3. **Activity Timeline**
   - Vertical list of activities
   - Activity type indicators
   - Timestamp and location
   - Interactive elements
   - Item height: 72dp

4. **Settings Section**
   - Icon + label format
   - Right chevron indicators
   - Toggle switches
   - Height: 56dp per item

## Main Profile Screen

```
┌─────────────────────────────────────────┐
│ ← Back                    ⚙️ Settings  │
├─────────────────────────────────────────┤
│                                        │
│           [Profile Photo]              │
│                                        │
│           John Doe                     │
│           @johndoe                     │
│                                        │
│ Profile Completion: 85%                │
│ [============================----]      │
│                                        │
│ 📍 New York City                       │
│ 📅 Joined January 2024                 │
│                                        │
│ ┌─────┐  ┌─────┐  ┌─────┐             │
│ │ 120 │  │ 450 │  │ 89  │             │
│ │Posts│  │Likes│  │Cmts │             │
│ └─────┘  └─────┘  └─────┘             │
│                                        │
│ Achievements                           │
│ ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐       │
│ │ 🏆  │ │ 🌟  │ │ 📝  │ │ 💬  │       │
│ │ Pro │ │ Top │ │ Auth│ │ Soc │       │
│ └─────┘ └─────┘ └─────┘ └─────┘  >    │
│                                        │
│ Recent Activity                        │
│ ┌─────────────────────────────────────┐ │
│ │ 📝 Posted "Local Event Update"      │ │
│ │ 2 hours ago                         │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ ❤️ Liked "Community Clean-up"       │ │
│ │ 5 hours ago                         │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ My Posts                              │
│ [Grid of Post Previews]               │
└─────────────────────────────────────────┘
```

## Edit Profile Screen

```
┌─────────────────────────────────────────┐
│ ← Back                      Save       │
├─────────────────────────────────────────┤
│                                        │
│           [Profile Photo]              │
│           [Change Photo]               │
│                                        │
│ Display Name                           │
│ ┌─────────────────────────────────────┐ │
│ │ John Doe                           │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Username                               │
│ ┌─────────────────────────────────────┐ │
│ │ @johndoe                           │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Bio                                    │
│ ┌─────────────────────────────────────┐ │
│ │ Community enthusiast and local      │ │
│ │ event organizer.                    │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Location                               │
│ ┌─────────────────────────────────────┐ │
│ │ New York City                       │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Email                                  │
│ ┌─────────────────────────────────────┐ │
│ │ john.doe@email.com                  │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Phone                                  │
│ ┌─────────────────────────────────────┐ │
│ │ +1 (555) 123-4567                  │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Settings Screen

```
┌─────────────────────────────────────────┐
│ ← Back                                 │
├─────────────────────────────────────────┤
│ Account Settings                        │
│ ┌─────────────────────────────────────┐ │
│ │ 🔒 Privacy                     >    │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔔 Notifications              >    │ │
│ ├─────────────────────────────────────┤ │
│ │ 🌍 Language & Region          >    │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Preferences                            │
│ ┌─────────────────────────────────────┐ │
│ │ 🌙 Dark Mode              [ON]      │ │
│ ├─────────────────────────────────────┤ │
│ │ 📱 Push Notifications    [ON]      │ │
│ ├─────────────────────────────────────┤ │
│ │ 📍 Location Services    [ON]      │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Support                                │
│ ┌─────────────────────────────────────┐ │
│ │ ❓ Help Center                 >    │ │
│ ├─────────────────────────────────────┤ │
│ │ 📝 Terms of Service           >    │ │
│ ├─────────────────────────────────────┤ │
│ │ 🔒 Privacy Policy            >    │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Log Out]                             │
└─────────────────────────────────────────┘
```

## Privacy Settings Screen

```
┌─────────────────────────────────────────┐
│ ← Back                                 │
├─────────────────────────────────────────┤
│ Profile Privacy                         │
│ ┌─────────────────────────────────────┐ │
│ │ Profile Visibility                  │ │
│ │ ○ Public                           │ │
│ │ ● Private                          │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Location Sharing                       │
│ ┌─────────────────────────────────────┐ │
│ │ Show my location            [ON]    │ │
│ │ Show my city only          [OFF]   │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Activity Visibility                    │
│ ┌─────────────────────────────────────┐ │
│ │ Show my posts              [ON]    │ │
│ │ Show my likes              [ON]    │ │
│ │ Show my comments           [OFF]   │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Data Usage                             │
│ ┌─────────────────────────────────────┐ │
│ │ Analytics Collection       [ON]    │ │
│ │ Personalized Content      [OFF]   │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Achievements Screen

```
┌─────────────────────────────────────────┐
│ ← Back                                 │
├─────────────────────────────────────────┤
│ My Achievements                         │
│                                        │
│ Recent                                 │
│ ┌─────────────────────────────────────┐ │
│ │ 🏆 Local Hero                       │ │
│ │ Earned 2 days ago                   │ │
│ │ [Share Achievement]                 │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Progress                               │
│ ┌─────────────────────────────────────┐ │
│ │ 📝 Content Creator                  │ │
│ │ [===========------] 70%            │ │
│ │ Create 50 posts                     │ │
│ └─────────────────────────────────────┘ │
│ ┌─────────────────────────────────────┐ │
│ │ 💬 Community Voice                  │ │
│ │ [================--] 90%           │ │
│ │ Receive 100 likes                   │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Completed                              │
│ ┌─────────────────────────────────────┐ │
│ │ 🌟 First Post                       │ │
│ │ Create your first post              │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## Loading State

```
┌─────────────────────────────────────────┐
│                                        │
│           [Shimmer Effect]             │
│                                        │
│           [Profile Photo]              │
│                                        │
│           [Loading Name...]            │
│           [Loading Username...]        │
│                                        │
│ [Loading Stats...]                     │
│                                        │
│ [Loading Achievements...]              │
│                                        │
│ [Loading Activity...]                  │
│                                        │
└─────────────────────────────────────────┘
```

## Error State

```
┌─────────────────────────────────────────┐
│                                        │
│           [Error Icon]                 │
│                                        │
│     Unable to load profile             │
│                                        │
│     Please check your connection       │
│     and try again                      │
│                                        │
│           [Retry Button]               │
│                                        │
└─────────────────────────────────────────┘
```

## Interactions

### Profile Actions
- Tap avatar to view/change photo
- Pull to refresh profile data
- Swipe between different sections
- Double tap to like posts
- Long press for quick actions

### Navigation
- Smooth transitions between screens
- Gesture-based navigation
- Bottom sheet for quick settings
- Modal dialogs for confirmations
- Back gesture support

### Animations
- Profile photo zoom/fade
- Achievement unlock animations
- Progress bar animations
- Loading state transitions
- Like/comment interactions

## Accessibility

### Screen Reader Support
- Content descriptions for images
- Action descriptions for buttons
- Achievement announcements
- Progress updates
- Error notifications

### Visual Accessibility
- High contrast mode
- Adjustable text size
- Color blind friendly
- Focus indicators
- Touch target size

### Interaction Accessibility
- Voice commands
- Keyboard navigation
- Reduced motion
- Alternative text
- Haptic feedback

## Analytics Integration

### User Engagement
- Profile view time
- Edit frequency
- Achievement progress
- Feature usage
- Settings changes

### Performance Metrics
- Load time
- Action response time
- Error rate
- Cache hit rate
- Animation frame rate

### User Behavior
- Most viewed sections
- Common settings
- Privacy preferences
- Achievement completion
- Content interaction

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
- Profile Name: Roboto Bold, 24sp
- Username: Roboto Regular, 16sp
- Section Headers: Roboto Medium, 20sp
- Body Text: Roboto Regular, 16sp
- Caption: Roboto Regular, 14sp
- Button Text: Roboto Medium, 16sp

### Components
1. **Profile Header**
   - Large avatar (120dp x 120dp)
   - Name and username stack
   - Stats row with dividers
   - Location and join date

2. **Achievement Cards**
   - Icon size: 32dp
   - Card height: 72dp
   - Progress indicator
   - Label and description

3. **Activity Items**
   - Icon size: 24dp
   - Item height: 72dp
   - Timestamp and location
   - Interactive elements

4. **Settings Items**
   - Icon size: 24dp
   - Item height: 56dp
   - Right-aligned controls
   - Dividers between sections 