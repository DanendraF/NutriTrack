# Onboarding UI - Simplified Version

## Changes Made

### Before: Multi-Step Flow (5 screens)
```
Welcome → Gender/Age → Measurements → Activity → Goal → Result
  (1/5)      (2/5)         (3/5)        (4/5)    (5/5)
```

### After: Single Screen (Simple)
```
Complete Profile (All in one)
```

## Why Simplified?

1. **Faster Testing** - Dapat langsung test backend integration tanpa navigate banyak screens
2. **Better UX** - User dapat lihat semua fields sekaligus, tidak perlu next-next-next
3. **Easier to Debug** - Semua data dalam 1 form, lebih mudah track errors
4. **Mobile-First** - Scroll form lebih natural di mobile daripada multi-step

## New UI Components

### Single Screen: `SimpleOnboardingScreen.kt`

**Features:**
- ✅ All fields dalam satu scrollable form
- ✅ Auto-fill email dari Firebase
- ✅ Real-time validation
- ✅ Loading state saat save
- ✅ Error handling dengan Card merah
- ✅ Auto-navigate ke dashboard on success

**Fields:**
1. Name (text input)
2. Email (read-only, from Firebase)
3. Gender (Male/Female chips)
4. Age (number input)
5. Height (decimal input)
6. Weight (decimal input)
7. Activity Level (4 options dengan deskripsi)
8. Goal (3 options: Lose/Maintain/Gain)

**Submit Button:**
- Shows loading spinner saat API call
- Disabled saat loading
- Auto-complete onboarding saat success

## Files Modified

1. **Created:** `SimpleOnboardingScreen.kt` - New single-screen UI
2. **Updated:** `OnboardingNavHost.kt` - Changed to use SimpleOnboardingScreen
3. **Kept:** Old multi-step screens (commented out, can restore if needed)

## Testing Flow

### Old Flow (Multi-Step):
```
1. Welcome screen
2. Click "Get Started"
3. Fill gender & age → Next
4. Fill height & weight → Next
5. Select activity level → Next
6. Select goal → Next
7. See result → Finish
```

### New Flow (Simple):
```
1. Fill all fields in one screen
2. Click "Complete Setup"
3. Done! (Auto navigate to dashboard)
```

## API Integration

Flow remains the same:
```
User fills form → Click submit → API call → Backend calculates BMR/TDEE →
Save to Firestore → Save to Room → Show dashboard
```

**Backend receives:**
- email, name, dateOfBirth (calculated from age), gender, height, weight

**Backend returns:**
- User data with calculated goals (BMR, TDEE, target calories, macros)

## Reverting to Multi-Step

If needed, uncomment code in `OnboardingNavHost.kt`:

```kotlin
// Change this:
startDestination = OnboardingRoutes.SIMPLE_ONBOARDING

// To this:
startDestination = OnboardingRoutes.WELCOME

// And uncomment the old NavHost composables
```

## Screenshots Concept

**Simple Onboarding Screen:**
```
┌─────────────────────────────────────┐
│ Complete Your Profile               │
├─────────────────────────────────────┤
│ Let's get to know you               │
│ We need some basic information...   │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Full Name                       │ │
│ │ [_________________________]     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Email                           │ │
│ │ test@example.com (disabled)     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Gender                              │
│ [Male]  [Female]                    │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Age (years)                     │ │
│ │ [_________________________]     │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ... (scroll for more)               │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │   Complete Setup                │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## Benefits for Testing

1. **Single Page Load** - Tidak perlu navigate untuk test
2. **Faster Input** - Copy-paste test data lebih cepat
3. **See All Errors** - Validation errors visible sekaligus
4. **Quick Retry** - Jika error, tidak perlu ulangi dari awal

## Next Steps

Sekarang siap untuk:
1. ✅ Run app di Android Studio
2. ✅ Test user registration dengan backend
3. ✅ Verify data masuk ke Firestore
4. ✅ Check calculated BMR/TDEE correct
