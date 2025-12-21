# Android-Backend Connection Testing Guide

## Overview
Dokumen ini menjelaskan cara testing koneksi antara Android app (NutriTrack) dengan Golang backend.

## Prerequisites

### 1. Backend Running
Backend harus running di `localhost:8080`:

```bash
cd nutritrack-backend
go run cmd/api/main.go
```

Pastikan muncul output:
```
🔧 Starting NutriTrack Backend...
📝 Environment: development
🔥 Initializing Firebase Admin SDK...
✅ Firebase initialized successfully
📦 Initializing repositories...
🌱 Checking food database...
✅ Seeded 29 foods
🚀 Server starting on :8080
```

### 2. Firebase Setup
- User harus login via Firebase Authentication
- App akan auto-inject Firebase ID token ke semua API requests via `AuthInterceptor`

### 3. Network Configuration
- **Android Emulator**: Backend URL = `http://10.0.2.2:8080/` (sudah di-configure di KoinModule)
- **Physical Device**: Ubah base URL ke IP komputer (e.g., `http://192.168.1.100:8080/`)

## Testing Flow

### Step 1: Start Backend

```bash
cd d:\UII\5\Pengembangan Aplikasi Bergerak\ProjectAkhir_Nutritrack\nutritrack-backend
go run cmd/api/main.go
```

### Step 2: Run Android App

#### Option A: Android Studio
1. Open project di Android Studio
2. Sync Gradle
3. Run app di emulator atau device
4. **Tidak perlu build APK** (karena gradle daemon crash issue saat assembling)

#### Option B: Command Line (Kompilasi Only)
```bash
cd NutriTrack
./gradlew compileDebugKotlin
```

### Step 3: Test User Registration Flow

1. **Buka app** → Welcome screen
2. **Click "Get Started"** → Login/Register
3. **Login dengan Firebase** (Google/Email)
4. **Complete onboarding**:
   - Name: "Test User"
   - Gender: Male
   - Age: 25
   - Height: 170 cm
   - Weight: 70 kg
   - Activity Level: Moderately Active
   - Goal: Maintain Weight
5. **Click "Finish"**

**Backend akan:**
- Receive POST request ke `/api/v1/users`
- Calculate BMR/TDEE
- Save user data ke Firestore
- Return calculated nutrition goals

**App akan:**
- Show calculated targets (calories, macros)
- Save to local Room database (offline backup)
- Navigate to dashboard

### Step 4: Check Backend Logs

Backend akan show output seperti:

```
2025-12-20T19:00:00+0700 [INFO] POST /api/v1/users 201 234ms
```

### Step 5: Verify Data di Firestore

1. Buka Firebase Console
2. Go to Firestore Database
3. Check collection `users` → User ID → Document data

Should see:
```json
{
  "id": "firebase_user_id",
  "email": "test@example.com",
  "name": "Test User",
  "gender": "male",
  "measurements": {
    "weight": 70,
    "height": 170,
    "dateOfBirth": "2000-01-01"
  },
  "goals": {
    "bmr": 1680.5,
    "tdee": 2327.5,
    "targetCalories": 2327,
    "targetProtein": 174.5,
    "targetCarbs": 232.7,
    "targetFat": 77.5
  },
  "createdAt": "2025-12-20T12:00:00Z"
}
```

## Testing Food Search

### Via App:
1. Navigate to Food screen
2. Search for "nasi"
3. Should see results from backend API

### Via API Direct:

#### Windows PowerShell:
```powershell
cd nutritrack-backend
.\test.ps1
# Choose option 5: Search Foods
```

#### Linux/Mac:
```bash
cd nutritrack-backend
./test.sh
# Choose option 5: Search Foods
```

## Debugging

### Check Backend Connection

**Test health endpoint:**

```bash
# From Android emulator
curl http://10.0.2.2:8080/health

# From host machine
curl http://localhost:8080/health
```

Expected response:
```json
{"status": "ok"}
```

### Check Authentication

**Enable HTTP logging** (already configured in KoinModule):

```kotlin
// KoinModule.kt
single {
    HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY  // ← Will show all requests
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
}
```

**Logcat output** akan show:
```
D/OkHttp: --> POST http://10.0.2.2:8080/api/v1/users
D/OkHttp: Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
D/OkHttp: Content-Type: application/json
D/OkHttp: {"email":"test@example.com","name":"Test User"...}
D/OkHttp: <-- 201 Created (234ms)
```

### Common Issues

#### 1. Connection Refused
**Error:** `Unable to resolve host "10.0.2.2": No address associated with hostname`

**Solution:**
- Backend belum running
- Start backend: `go run cmd/api/main.go`

#### 2. 401 Unauthorized
**Error:** `{"error": "Unauthorized"}`

**Solution:**
- User belum login Firebase
- Token expired → Logout & login again
- Check `AuthInterceptor` adding token correctly

#### 3. 500 Internal Server Error
**Error:** `{"error": "Internal server error"}`

**Solution:**
- Check backend logs untuk error details
- Biasanya Firebase/Firestore connection issue
- Verify `serviceAccountKey.json` exists

#### 4. Network Timeout
**Error:** `SocketTimeoutException`

**Solution:**
- Backend slow/crashed
- Increase timeout di `KoinModule.kt`:
  ```kotlin
  .connectTimeout(60, TimeUnit.SECONDS)  // Default: 30s
  ```

## Code Changes Summary

### Files Modified/Created:

1. **Networking Setup**
   - `app/build.gradle.kts` - Added Retrofit dependencies, enabled BuildConfig
   - `gradle.properties` - Increased JVM memory to 4GB

2. **API Layer**
   - `data/remote/Result.kt` - Sealed class for async states
   - `data/remote/dto/UserDto.kt` - User request/response DTOs
   - `data/remote/dto/FoodDto.kt` - Food request/response DTOs
   - `data/remote/api/NutriTrackApiService.kt` - Retrofit interface
   - `data/remote/interceptor/AuthInterceptor.kt` - Firebase token injection

3. **Repository Layer**
   - `data/repository/ApiUserRepository.kt` - User API operations
   - `data/repository/ApiFoodRepository.kt` - Food API operations

4. **DI Setup**
   - `di/KoinModule.kt` - Registered all networking components

5. **ViewModel Integration**
   - `presentation/onboarding/viewmodel/OnboardingViewModel.kt` - Updated to use API

## Next Steps

1. ✅ **Backend Integration** - DONE
2. ✅ **ViewModel Update** - DONE
3. ⏳ **End-to-End Testing** - IN PROGRESS
4. ⏳ **Offline/Online Sync** - TODO
5. ⏳ **Error Handling UI** - TODO

## API Endpoints Used

| Endpoint | Method | Purpose | Status |
|----------|--------|---------|--------|
| `/health` | GET | Health check | ✅ Ready |
| `/api/v1/users` | POST | Create user | ✅ Integrated |
| `/api/v1/users/me` | GET | Get current user | ⏳ TODO |
| `/api/v1/foods` | GET | Search foods | ⏳ TODO |
| `/api/v1/foods/:id` | GET | Get food details | ⏳ TODO |
| `/api/v1/foods/barcode/:code` | GET | Lookup by barcode | ⏳ TODO |

## Performance Notes

- **Gradle Memory**: Increased to 4GB (dari 2GB) untuk handle large dependencies
- **Build Time**: ~1-2 minutes untuk `compileDebugKotlin`
- **APK Assembly**: Crashes due to DEX memory → Use Android Studio untuk run
- **Network Timeout**: 30 seconds (configurable)

## References

- Backend API Documentation: `nutritrack-backend/TESTING.md`
- API Summary: `nutritrack-backend/API_SUMMARY.md`
- Sprint Progress: `PROGRESS.md`
