# TownTalk Authentication Screens

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
   - Single-line title
   - Back button on the left (when applicable)
   - Elevation: 0dp
   - Height: 56dp

2. **Text Fields**
   - Outlined style
   - Label text above input
   - Error state with red text and outline
   - Password visibility toggle
   - Clear text button
   - Height: 56dp

3. **Buttons**
   - Primary: Filled with Primary color
   - Secondary: Outlined with Secondary color
   - Text: Text only with Primary color
   - Height: 48dp
   - Corner radius: 8dp

4. **Social Login Buttons**
   - Icon + text combination
   - Brand colors for each provider
   - Height: 48dp
   - Corner radius: 8dp

5. **Progress Indicator**
   - Circular progress for loading states
   - Linear progress for multi-step processes
   - Primary color

## Login Screen

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     Welcome Back!                      │
│                                        │
│ Email                                   │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Password                               │
│ ┌─────────────────────────────────────┐ │
│ │ ••••••••  👁️ Show                 │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Forgot Password?                       │
│                                        │
│ [Sign In Button]                       │
│                                        │
│ or continue with                       │
│                                        │
│ [Google Sign In]  [Apple Sign In]      │
│                                        │
│ Don't have an account? Sign Up         │
└─────────────────────────────────────────┘
```

## Registration Screen

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     Create Account                     │
│                                        │
│ Full Name                              │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Email                                   │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Password                               │
│ ┌─────────────────────────────────────┐ │
│ │ ••••••••  👁️ Show                 │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ Password Strength: [====----] Medium   │
│                                        │
│ Terms of Service and Privacy Policy    │
│ [ ] I agree to the terms               │
│                                        │
│ [Create Account Button]                │
│                                        │
│ Already have an account? Sign In       │
└─────────────────────────────────────────┘
```

## Phone Verification Screen

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     Phone Verification                 │
│                                        │
│ Enter your phone number to receive      │
│ a verification code                    │
│                                        │
│ Phone Number                           │
│ ┌─────────────────────────────────────┐ │
│ │ +1 (___) ___-____                  │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Send Code Button]                     │
│                                        │
│ or continue with                       │
│                                        │
│ [Google Sign In]  [Apple Sign In]      │
│                                        │
│ Already have an account? Sign In       │
└─────────────────────────────────────────┘
```

## Verification Code Screen

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     Enter Verification Code            │
│                                        │
│ We've sent a code to                    │
│ +1 (555) 123-4567                      │
│                                        │
│ ┌─┐ ┌─┐ ┌─┐ ┌─┐ ┌─┐ ┌─┐               │
│ │ │ │ │ │ │ │ │ │ │ │ │               │
│ └─┘ └─┘ └─┘ └─┘ └─┘ └─┘               │
│                                        │
│ [Verify Button]                        │
│                                        │
│ Didn't receive the code?               │
│ [Resend Code] (30s)                    │
│                                        │
│ [Change Phone Number]                  │
└─────────────────────────────────────────┘
```

## Forgot Password Screen

```
┌─────────────────────────────────────────┐
│ ← Back                                 │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     Reset Password                     │
│                                        │
│ Enter your email address to receive     │
│ a password reset link                  │
│                                        │
│ Email                                   │
│ ┌─────────────────────────────────────┐ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                        │
│ [Send Reset Link Button]               │
│                                        │
│ Remember your password?                │
│ [Sign In]                             │
└─────────────────────────────────────────┘
```

## Loading State

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     [Circular Progress]                │
│                                        │
│     Signing in...                      │
│                                        │
└─────────────────────────────────────────┘
```

## Error State

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     [Error Icon]                       │
│                                        │
│     Invalid email or password          │
│                                        │
│     Please try again                   │
│                                        │
│     [Retry Button]                     │
│                                        │
└─────────────────────────────────────────┘
```

## Success State

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [App Logo]                         │
│                                        │
│     [Success Icon]                     │
│                                        │
│     Account created successfully!      │
│                                        │
│     Welcome to TownTalk                │
│                                        │
│     [Continue Button]                  │
│                                        │
└─────────────────────────────────────────┘
```

## Interactions

### Form Validation
- Real-time validation as user types
- Visual feedback for valid/invalid fields
- Error messages appear below fields
- Submit button enabled only when form is valid
- Password strength indicator
- Email format validation
- Phone number format validation

### Authentication Methods
- Email/Password login
- Google Sign-In
- Apple Sign-In
- Phone number verification
- Biometric authentication (fingerprint/face)

### Security Features
- Password visibility toggle
- Auto-logout after inactivity
- Session management
- Secure token storage
- Rate limiting for verification codes
- CAPTCHA for suspicious activity

### Navigation
- Smooth transitions between screens
- Back gesture support
- Clear error recovery paths
- Skip options where appropriate
- Progress indicators for multi-step processes

## Accessibility

### Screen Reader Support
- Content descriptions for all images
- Action descriptions for buttons
- Error announcements
- Form field labels
- Success/failure notifications

### Visual Accessibility
- High contrast mode support
- Adjustable text size
- Color blind friendly palette
- Clear focus indicators
- Sufficient color contrast

### Interaction Accessibility
- Large touch targets (minimum 48dp)
- Keyboard navigation support
- Voice control support
- Reduced motion option
- Alternative text for images

## Performance Considerations

### Loading States
- Skeleton screens for content loading
- Progress indicators for operations
- Optimistic UI updates
- Background data prefetching
- Cached credentials

### Error Handling
- Graceful error recovery
- Offline mode support
- Retry mechanisms
- Clear error messages
- Fallback options

### Animation Performance
- Hardware acceleration
- Frame rate monitoring
- Memory usage optimization
- Battery usage consideration
- Reduced animations option

## Analytics Integration

### User Engagement
- Login success rate
- Registration completion rate
- Authentication method preference
- Time to complete registration
- Drop-off points

### Performance Metrics
- Authentication time
- Error rate
- Retry rate
- Session duration
- Logout frequency

### Security Metrics
- Failed login attempts
- Password reset requests
- Account lockouts
- Suspicious activity
- Verification code usage

## Biometric Authentication

```
┌─────────────────────────────────────────┐
│ TownTalk                                │
├─────────────────────────────────────────┤
│                                        │
│     [Fingerprint Icon]                 │
│                                        │
│     Sign in with fingerprint           │
│                                        │
│     Place your finger on the sensor    │
│                                        │
│     [Cancel]                           │
│                                        │
└─────────────────────────────────────────┘
```

## Social Login Buttons

```
┌─────────────────────────────────────────┐
│ [Google Logo] Sign in with Google       │
├─────────────────────────────────────────┤
│ [Apple Logo] Sign in with Apple         │
└─────────────────────────────────────────┘
```

## Password Strength Indicator

```
┌─────────────────────────────────────────┐
│ Password Strength:                      │
│ [====----] Medium                       │
│                                        │
│ • At least 8 characters                │
│ • At least one uppercase letter        │
│ • At least one number                  │
│ • At least one special character       │
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
- Error: #D32F2F (Red)
- Success: #4CAF50 (Green)

### Typography
- Headings: Roboto Bold, 24sp
- Body: Roboto Regular, 16sp
- Caption: Roboto Regular, 14sp
- Button: Roboto Medium, 16sp

### Components
1. **Text Fields**
   - Outlined style
   - Label text above input
   - Error state with red text and outline
   - Password visibility toggle
   - Clear text button

2. **Buttons**
   - Primary: Filled with Primary color
   - Secondary: Outlined with Secondary color
   - Text: Text only with Primary color
   - Social login buttons with brand colors

3. **Progress Indicators**
   - Circular progress for loading states
   - Linear progress for multi-step processes
   - Password strength indicator

4. **Icons**
   - App logo
   - Social login provider logos
   - Action icons (show/hide password, clear text)
   - Status icons (success, error, loading)

### Interactions
1. **Form Validation**
   - Real-time validation as user types
   - Visual feedback for valid/invalid fields
   - Error messages appear below fields
   - Submit button enabled only when form is valid

2. **Authentication Methods**
   - Email/Password login
   - Google Sign-In
   - Apple Sign-In
   - Phone number verification
   - Biometric authentication

3. **Security Features**
   - Password visibility toggle
   - Auto-logout after inactivity
   - Session management
   - Secure token storage
   - Rate limiting for verification codes
``` 