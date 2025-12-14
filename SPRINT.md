# NutriTrack - Sprint Planning & Development Roadmap

## Project Overview
**NutriTrack** adalah aplikasi mobile nutrition tracking yang membantu users untuk:
- Scan makanan menggunakan AI/Barcode
- Track kalori dan nutrisi harian
- Mendapatkan personalized nutrition goals
- Melihat insights dan tips kesehatan

**Current Status:** MVP Frontend Complete (Kotlin + Jetpack Compose)
**Target:** Full-stack app dengan Golang Backend + Firebase

---

## 📊 Development Phases Overview

| Phase | Focus Area | Duration | Status |
|-------|-----------|----------|--------|
| **Fase 1** | Penyempurnaan Frontend (Kotlin) | Sprint 1-2 | 🟡 Pending |
| **Fase 2** | Backend Setup (Golang + Firebase) | Sprint 3-4 | ⚪ Not Started |
| **Fase 3** | Frontend-Backend Integration | Sprint 5 | ⚪ Not Started |
| **Fase 4** | Advanced Features | Sprint 6-8 | ⚪ Not Started |
| **Fase 5** | Testing & Deployment | Sprint 9-10 | ⚪ Not Started |

---

# FASE 1: PENYEMPURNAAN FRONTEND (Kotlin/Android)

## 🏃 Sprint 1: Foundation & Persistence Layer
**Duration:** 2 weeks
**Goal:** Setup local database, dependency injection, dan refactor arsitektur

### Week 1: Setup Infrastructure

#### 📋 Tasks:

**1. Setup Hilt Dependency Injection** ⏱️ 1 day
- [ ] Add Hilt dependencies ke `build.gradle.kts`:
  ```kotlin
  implementation "com.google.dagger:hilt-android:2.50"
  kapt "com.google.dagger:hilt-compiler:2.50"
  implementation "androidx.hilt:hilt-navigation-compose:1.1.0"
  ```
- [ ] Create `NutriTrackApplication.kt` dengan `@HiltAndroidApp`
- [ ] Update `AndroidManifest.xml` dengan application class
- [ ] Create `AppModule.kt` untuk app-level dependencies
- [ ] Add `@AndroidEntryPoint` ke `MainActivity`

**2. Setup Room Database** ⏱️ 2 days
- [ ] Add Room dependencies:
  ```kotlin
  implementation "androidx.room:room-runtime:2.6.1"
  implementation "androidx.room:room-ktx:2.6.1"
  kapt "androidx.room:room-compiler:2.6.1"
  ```
- [ ] Create entities:
  - [ ] `UserEntity.kt` - User profile data
  - [ ] `OnboardingDataEntity.kt` - Onboarding information
  - [ ] `FoodEntity.kt` - Food items
  - [ ] `MealEntity.kt` - Meal logs
  - [ ] `DailyLogEntity.kt` - Daily nutrition summary
- [ ] Create DAOs:
  - [ ] `UserDao.kt`
  - [ ] `FoodDao.kt`
  - [ ] `MealDao.kt`
  - [ ] `DailyLogDao.kt`
- [ ] Create `NutriTrackDatabase.kt` dengan Room database
- [ ] Create `DatabaseModule.kt` untuk Hilt provision

**3. Refactor Project Structure** ⏱️ 1 day
- [ ] Create new package structure:
  ```
  com.example.nutritrack/
  ├── data/
  │   ├── local/
  │   │   ├── dao/
  │   │   ├── entity/
  │   │   └── NutriTrackDatabase.kt
  │   └── repository/
  ├── domain/
  │   ├── model/
  │   └── usecase/
  ├── presentation/
  │   ├── auth/
  │   ├── onboarding/
  │   ├── home/
  │   ├── food/
  │   ├── scan/
  │   └── tips/
  ├── di/
  └── utils/
  ```
- [ ] Move existing screens ke package yang sesuai
- [ ] Create domain models (non-entity classes)

**4. Implement Repository Pattern** ⏱️ 1 day
- [ ] Create `UserRepository.kt` dengan interface & implementation
- [ ] Create `FoodRepository.kt`
- [ ] Create `MealRepository.kt`
- [ ] Inject repositories dengan Hilt di `RepositoryModule.kt`

### Week 2: State Management & Data Flow

**5. Create ViewModels untuk All Screens** ⏱️ 2 days
- [ ] Refactor `OnboardingViewModel` dengan Repository
- [ ] Create `LoginViewModel` dengan validation
- [ ] Create `RegisterViewModel` dengan validation
- [ ] Create `HomeViewModel` dengan daily stats
- [ ] Create `FoodViewModel` untuk food tracking
- [ ] Create `ScanViewModel` untuk scan flow
- [ ] Create `TipsViewModel` untuk tips & articles
- [ ] Inject repositories ke ViewModels

**6. Implement Nutrition Calculation Engine** ⏱️ 2 days
- [ ] Create `NutritionCalculator.kt` utility class:
  - [ ] Calculate BMR (Basal Metabolic Rate) dengan Harris-Benedict equation
  - [ ] Calculate TDEE (Total Daily Energy Expenditure)
  - [ ] Calculate macro distribution (protein, carbs, fat) based on goal
  - [ ] Calculate daily calorie target
- [ ] Create `ActivityLevel` enum dengan multipliers
- [ ] Create `NutritionGoal` enum (lose/maintain/gain weight)
- [ ] Integrate calculation ke `OnboardingViewModel`
- [ ] Update `CalculationResultScreen` dengan real calculations

**7. Implement Data Persistence** ⏱️ 1 day
- [ ] Save onboarding data ke Room saat completion
- [ ] Save user preferences
- [ ] Implement auto-login check (skip auth jika user sudah login)
- [ ] Implement data loading di app startup
- [ ] Add loading states ke ViewModels

**8. Error Handling & Loading States** ⏱️ 1 day
- [ ] Create `UiState` sealed class untuk screens:
  ```kotlin
  sealed class UiState<T> {
      class Loading : UiState<Nothing>()
      data class Success<T>(val data: T) : UiState<T>()
      data class Error(val message: String) : UiState<Nothing>()
  }
  ```
- [ ] Add loading indicators ke screens
- [ ] Add error messages dengan Snackbar/Dialog
- [ ] Implement retry mechanisms

### Sprint 1 Deliverables:
✅ Hilt DI fully integrated
✅ Room Database working dengan all entities
✅ Repository pattern implemented
✅ All screens have ViewModels
✅ Nutrition calculation working
✅ Local data persistence working
✅ Better error handling

---

## 🏃 Sprint 2: ML Integration & Missing Screens
**Duration:** 2 weeks
**Goal:** Wire ML Kit food detection, implement profile screen, enhance UI

### Week 1: ML Kit Integration

**1. Wire ML Kit Image Labeling** ⏱️ 2 days
- [ ] Create `FoodDetectionService.kt`:
  - [ ] Process captured image dengan ML Kit Image Labeling
  - [ ] Extract labels dengan confidence scores
  - [ ] Map labels ke food database
- [ ] Create local food database (JSON/Room):
  - [ ] Common Indonesian foods dengan nutrition data
  - [ ] Mapping dari ML labels ke food items
- [ ] Integrate detection ke `ScanViewModel`
- [ ] Update `ScanResultSheet` dengan detected food info
- [ ] Add loading state saat processing image

**2. Implement Barcode Scanning** ⏱️ 1 day
- [ ] Wire ML Kit Barcode Scanner ke Camera mode
- [ ] Create `BarcodeService.kt` untuk process barcode
- [ ] Integrate dengan Open Food Facts API:
  ```kotlin
  implementation "com.squareup.retrofit2:retrofit:2.9.0"
  implementation "com.squareup.retrofit2:converter-gson:2.9.0"
  ```
- [ ] Create fallback jika barcode tidak ditemukan
- [ ] Cache barcode results locally

**3. Enhance Food Database** ⏱️ 1 day
- [ ] Create comprehensive local food database:
  - [ ] 100+ common Indonesian foods
  - [ ] Nutrition data (calories, protein, carbs, fat, fiber)
  - [ ] Portion sizes (gram, piece, cup, etc.)
- [ ] Create `FoodDatabaseInitializer.kt`
- [ ] Seed database on first app launch
- [ ] Add search functionality

**4. Implement Portion Size Adjustment** ⏱️ 1 day
- [ ] Make portion stepper functional di `FoodScreen`
- [ ] Calculate nutrition values based on portion
- [ ] Update UI dengan recalculated values
- [ ] Add common portion presets (100g, 1 cup, 1 piece)

### Week 2: Complete Missing Features

**5. Implement Profile Screen** ⏱️ 2 days
- [ ] Create `ProfileScreen.kt`:
  - [ ] Display user info (name, email, age, measurements)
  - [ ] Display current goals
  - [ ] Display progress statistics
  - [ ] Logout button
- [ ] Create `ProfileViewModel.kt`
- [ ] Add edit profile functionality:
  - [ ] Edit measurements (height, weight)
  - [ ] Edit activity level
  - [ ] Edit nutrition goal
  - [ ] Recalculate TDEE when goals change
- [ ] Add profile picture (camera/gallery picker)
- [ ] Navigate ke profile dari bottom nav

**6. Implement Settings Screen** ⏱️ 1 day
- [ ] Create `SettingsScreen.kt`:
  - [ ] Unit preferences (metric/imperial)
  - [ ] Notification settings
  - [ ] Theme settings (light/dark/system)
  - [ ] Language selection (future)
  - [ ] About app
  - [ ] Privacy policy
  - [ ] Terms of service
- [ ] Create `SettingsViewModel.kt`
- [ ] Save preferences dengan DataStore:
  ```kotlin
  implementation "androidx.datastore:datastore-preferences:1.0.0"
  ```

**7. Enhance Home Screen with Real Data** ⏱️ 2 days
- [ ] Load daily logs dari Room Database
- [ ] Calculate real-time calorie progress
- [ ] Display today's meals dari database
- [ ] Add meal management (edit/delete meals)
- [ ] Implement "Add Quick Meal" button
- [ ] Create basic chart untuk progress tracker:
  ```kotlin
  implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"
  ```
- [ ] Show weekly calorie trend

**8. Improve Food Logging Flow** ⏱️ 1 day
- [ ] Create `AddFoodScreen.kt` dengan search functionality
- [ ] Allow manual food entry
- [ ] Add meal type selection (breakfast, lunch, dinner, snack)
- [ ] Add timestamp untuk meals
- [ ] Create food history dengan recent/frequent foods
- [ ] Implement quick add dari history

### Sprint 2 Deliverables:
✅ ML Kit food detection working
✅ Barcode scanning functional
✅ Local food database dengan 100+ items
✅ Profile screen complete
✅ Settings screen complete
✅ Home screen showing real data
✅ Full food logging flow working
✅ Basic charts implemented

---

# FASE 2: BACKEND SETUP (Golang + Firebase)

## 🏃 Sprint 3: Firebase & Golang Backend Foundation
**Duration:** 2 weeks
**Goal:** Setup Firebase project, build Golang REST API foundation

### Week 1: Firebase Setup

**1. Firebase Project Setup** ⏱️ 0.5 day
- [ ] Create Firebase project di console.firebase.google.com
- [ ] Add Android app ke Firebase project
- [ ] Download `google-services.json`
- [ ] Add Firebase SDK ke Android app:
  ```kotlin
  implementation platform('com.google.firebase:firebase-bom:32.7.0')
  implementation 'com.google.firebase:firebase-auth-ktx'
  implementation 'com.google.firebase:firebase-firestore-ktx'
  implementation 'com.google.firebase:firebase-storage-ktx'
  implementation 'com.google.firebase:firebase-analytics-ktx'
  ```
- [ ] Enable Authentication methods (Email/Password, Google Sign-In)
- [ ] Setup Firestore Database (start in test mode)
- [ ] Setup Cloud Storage bucket

**2. Design Firestore Database Schema** ⏱️ 0.5 day
- [ ] Create database structure document
- [ ] Design collections:
  ```
  users/{userId}
    - profile: {name, email, dateOfBirth, gender}
    - measurements: {height, weight, updatedAt}
    - goals: {activityLevel, nutritionGoal, targetCalories, targetMacros}
    - settings: {units, notifications, theme}
    - createdAt, updatedAt

  daily_logs/{userId}/logs/{date}
    - date: YYYY-MM-DD
    - meals: [
        {id, foodId, foodName, mealType, quantity, portion, calories, macros, timestamp}
      ]
    - summary: {totalCalories, totalProtein, totalCarbs, totalFat}
    - createdAt, updatedAt

  foods/{foodId}
    - name, nameIndonesian
    - category
    - nutrition: {calories, protein, carbs, fat, fiber, sugar, sodium}
    - servingSize: {amount, unit}
    - barcode (optional)
    - imageUrl (optional)
    - isVerified, source
    - createdAt

  scanned_images/{userId}/images/{imageId}
    - imageUrl
    - detectedLabels: []
    - selectedFood
    - timestamp
  ```
- [ ] Create Firestore security rules
- [ ] Create indexes untuk queries

**3. Golang Project Initialization** ⏱️ 1 day
- [ ] Initialize Go project:
  ```bash
  mkdir nutritrack-backend
  cd nutritrack-backend
  go mod init github.com/yourusername/nutritrack-backend
  ```
- [ ] Create project structure:
  ```
  nutritrack-backend/
  ├── cmd/
  │   └── api/
  │       └── main.go
  ├── internal/
  │   ├── config/
  │   ├── handlers/
  │   ├── services/
  │   ├── models/
  │   ├── repository/
  │   └── middleware/
  ├── pkg/
  │   ├── firebase/
  │   └── utils/
  ├── api/
  │   └── openapi.yaml
  ├── .env.example
  ├── .gitignore
  ├── Dockerfile
  └── go.mod
  ```
- [ ] Install core dependencies:
  ```bash
  go get github.com/gin-gonic/gin
  go get firebase.google.com/go/v4
  go get cloud.google.com/go/firestore
  go get cloud.google.com/go/storage
  go get github.com/joho/godotenv
  go get github.com/golang-jwt/jwt/v5
  ```

**4. Setup Firebase Admin SDK** ⏱️ 1 day
- [ ] Create service account key dari Firebase console
- [ ] Create `pkg/firebase/firebase.go`:
  - [ ] Initialize Firebase App
  - [ ] Initialize Auth client
  - [ ] Initialize Firestore client
  - [ ] Initialize Storage client
- [ ] Create environment configuration (`internal/config/config.go`)
- [ ] Setup `.env` file dengan credentials

**5. Implement Authentication Middleware** ⏱️ 1 day
- [ ] Create `internal/middleware/auth.go`:
  - [ ] Verify Firebase ID tokens
  - [ ] Extract user ID dari token
  - [ ] Set user context untuk handlers
- [ ] Create CORS middleware
- [ ] Create logging middleware
- [ ] Create error handling middleware

### Week 2: Core API Endpoints

**6. Implement User Management Endpoints** ⏱️ 2 days
- [ ] Create `internal/models/user.go` dengan structs
- [ ] Create `internal/repository/user_repository.go`:
  - [ ] CreateUser
  - [ ] GetUserByID
  - [ ] UpdateUserProfile
  - [ ] UpdateUserGoals
  - [ ] DeleteUser
- [ ] Create `internal/services/user_service.go` untuk business logic
- [ ] Create `internal/handlers/user_handler.go`:
  - [ ] `GET /api/v1/users/me` - Get current user
  - [ ] `PUT /api/v1/users/me` - Update profile
  - [ ] `PUT /api/v1/users/me/goals` - Update nutrition goals
  - [ ] `DELETE /api/v1/users/me` - Delete account
- [ ] Add routes ke `main.go`

**7. Implement Food Database Endpoints** ⏱️ 2 days
- [ ] Create `internal/models/food.go`
- [ ] Create `internal/repository/food_repository.go`:
  - [ ] GetFoodByID
  - [ ] SearchFoods (by name, category)
  - [ ] GetFoodByBarcode
  - [ ] CreateFood (admin)
  - [ ] UpdateFood (admin)
- [ ] Create `internal/services/food_service.go`
- [ ] Create `internal/handlers/food_handler.go`:
  - [ ] `GET /api/v1/foods` - Search foods (query params)
  - [ ] `GET /api/v1/foods/:id` - Get food details
  - [ ] `GET /api/v1/foods/barcode/:code` - Lookup by barcode
  - [ ] `POST /api/v1/foods` - Create food (admin only)
- [ ] Seed initial food database (100+ Indonesian foods)

**8. Setup API Documentation** ⏱️ 1 day
- [ ] Create OpenAPI 3.0 spec (`api/openapi.yaml`)
- [ ] Document all endpoints dengan request/response schemas
- [ ] Add Swagger UI:
  ```bash
  go get github.com/swaggo/gin-swagger
  go get github.com/swaggo/files
  ```
- [ ] Generate Swagger docs
- [ ] Serve documentation di `/api/docs`

### Sprint 3 Deliverables:
✅ Firebase project configured
✅ Firestore database schema designed
✅ Golang backend project initialized
✅ Firebase Admin SDK integrated
✅ Authentication middleware working
✅ User management API complete
✅ Food database API complete
✅ API documentation available

---

## 🏃 Sprint 4: Advanced Backend Features
**Duration:** 2 weeks
**Goal:** Implement meal logging, food detection API, statistics

### Week 1: Meal Logging & Daily Logs

**1. Implement Daily Log Endpoints** ⏱️ 2 days
- [ ] Create `internal/models/meal.go` dan `daily_log.go`
- [ ] Create `internal/repository/meal_repository.go`:
  - [ ] GetDailyLog (by user & date)
  - [ ] AddMeal
  - [ ] UpdateMeal
  - [ ] DeleteMeal
  - [ ] GetMealHistory (date range)
- [ ] Create `internal/services/meal_service.go`:
  - [ ] Calculate daily totals
  - [ ] Update daily summary
  - [ ] Validate meal data
- [ ] Create `internal/handlers/meal_handler.go`:
  - [ ] `GET /api/v1/logs` - Get daily log (query: date)
  - [ ] `POST /api/v1/logs/meals` - Add meal
  - [ ] `PUT /api/v1/logs/meals/:id` - Update meal
  - [ ] `DELETE /api/v1/logs/meals/:id` - Delete meal
  - [ ] `GET /api/v1/logs/history` - Get logs range

**2. Implement Nutrition Calculation Service** ⏱️ 1 day
- [ ] Create `internal/services/nutrition_service.go`:
  - [ ] CalculateBMR (Harris-Benedict)
  - [ ] CalculateTDEE
  - [ ] CalculateMacros (based on goal)
  - [ ] CalculateProgress (current vs target)
- [ ] Add endpoint `POST /api/v1/nutrition/calculate` untuk goals
- [ ] Create recommendation logic

**3. Implement Cloud Storage Integration** ⏱️ 1 day
- [ ] Create `internal/services/storage_service.go`:
  - [ ] Upload image ke Cloud Storage
  - [ ] Generate signed URLs
  - [ ] Delete images
  - [ ] Image compression/optimization
- [ ] Create endpoint `POST /api/v1/upload` untuk images
- [ ] Set storage bucket CORS rules

### Week 2: Food Detection & Statistics

**4. Integrate Google Cloud Vision API** ⏱️ 2 days
- [ ] Enable Cloud Vision API di Google Cloud Console
- [ ] Install Vision SDK:
  ```bash
  go get cloud.google.com/go/vision/v2/apiv1
  ```
- [ ] Create `internal/services/vision_service.go`:
  - [ ] Analyze image dengan Label Detection
  - [ ] Analyze image dengan Object Localization
  - [ ] Extract food-related labels
  - [ ] Map labels ke food database
- [ ] Create `internal/handlers/scan_handler.go`:
  - [ ] `POST /api/v1/scan/analyze` - Analyze food image
  - [ ] Return detected foods dengan confidence scores
  - [ ] Save scan history ke Firestore

**5. Implement Barcode Lookup Service** ⏱️ 1 day
- [ ] Create `internal/services/barcode_service.go`:
  - [ ] Check local database first
  - [ ] Fallback ke Open Food Facts API
  - [ ] Cache results di Firestore
- [ ] Create endpoint `POST /api/v1/scan/barcode`
- [ ] Handle barcode not found errors

**6. Implement Statistics Endpoints** ⏱️ 2 days
- [ ] Create `internal/services/stats_service.go`:
  - [ ] CalculateWeeklyStats (avg calories, macros)
  - [ ] CalculateMonthlyStats
  - [ ] CalculateStreak (consecutive days logged)
  - [ ] GetNutrientTrends
- [ ] Create `internal/handlers/stats_handler.go`:
  - [ ] `GET /api/v1/stats/daily` - Today's summary
  - [ ] `GET /api/v1/stats/weekly` - Last 7 days
  - [ ] `GET /api/v1/stats/monthly` - Last 30 days
  - [ ] `GET /api/v1/stats/trends` - Nutrient trends

**7. Implement Tips & Recommendations** ⏱️ 1 day
- [ ] Create tips database di Firestore
- [ ] Create `internal/services/tips_service.go`:
  - [ ] GetDailyTip (random)
  - [ ] GetPersonalizedTips (based on user goals & logs)
  - [ ] GetNutrientDeficiencyAlerts
- [ ] Create endpoint `GET /api/v1/tips/daily`
- [ ] Seed tips database dengan content

### Sprint 4 Deliverables:
✅ Meal logging API complete
✅ Daily log tracking working
✅ Cloud Storage integration done
✅ Google Cloud Vision API integrated
✅ Food detection API working
✅ Barcode lookup service complete
✅ Statistics & analytics endpoints done
✅ Tips & recommendations API ready

---

# FASE 3: FRONTEND-BACKEND INTEGRATION

## 🏃 Sprint 5: Full Integration
**Duration:** 2 weeks
**Goal:** Connect Android app dengan Golang backend, implement sync

### Week 1: Authentication & Core Integration

**1. Setup Retrofit API Client** ⏱️ 1 day
- [ ] Add dependencies:
  ```kotlin
  implementation "com.squareup.retrofit2:retrofit:2.9.0"
  implementation "com.squareup.retrofit2:converter-gson:2.9.0"
  implementation "com.squareup.okhttp3:logging-interceptor:4.12.0"
  ```
- [ ] Create `data/remote/api/` package
- [ ] Create `NutriTrackApiService.kt` dengan all endpoints
- [ ] Create `ApiModule.kt` untuk Hilt
- [ ] Create interceptor untuk auth tokens
- [ ] Add network error handling

**2. Implement Firebase Authentication** ⏱️ 2 days
- [ ] Update `LoginViewModel`:
  - [ ] Firebase signInWithEmailAndPassword
  - [ ] Save user ID token
  - [ ] Call backend API untuk create/get user
- [ ] Update `RegisterViewModel`:
  - [ ] Firebase createUserWithEmailAndPassword
  - [ ] Call backend API untuk create user profile
- [ ] Implement auto-login dengan saved credentials
- [ ] Add Google Sign-In (optional):
  ```kotlin
  implementation 'com.google.android.gms:play-services-auth:20.7.0'
  ```
- [ ] Update navigation flow

**3. Update Repository Layer** ⏱️ 2 days
- [ ] Refactor `UserRepository`:
  - [ ] Add remote data source
  - [ ] Implement offline-first pattern (Room + Network)
  - [ ] Sync user data dengan backend
- [ ] Refactor `FoodRepository`:
  - [ ] Fetch foods dari backend
  - [ ] Cache di Room database
  - [ ] Search local first, fallback ke network
- [ ] Refactor `MealRepository`:
  - [ ] Save meals to both Room & Firestore
  - [ ] Implement conflict resolution

### Week 2: Advanced Features Integration

**4. Implement Food Detection Integration** ⏱️ 2 days
- [ ] Create `ScanRepository` dengan API calls
- [ ] Update `ScanViewModel`:
  - [ ] Upload image ke Cloud Storage
  - [ ] Call Vision API via backend
  - [ ] Process detection results
  - [ ] Map detected labels ke foods
- [ ] Update `ScanResultSheet` dengan backend data
- [ ] Add retry logic untuk failed detections
- [ ] Implement offline mode (use ML Kit only)

**5. Implement Data Synchronization** ⏱️ 2 days
- [ ] Create `SyncWorker` dengan WorkManager:
  ```kotlin
  implementation "androidx.work:work-runtime-ktx:2.9.0"
  ```
- [ ] Sync strategies:
  - [ ] Push local changes ke backend
  - [ ] Pull remote changes to local
  - [ ] Handle conflicts (last-write-wins)
- [ ] Schedule periodic sync (every 15 minutes)
- [ ] Add manual sync button di Settings
- [ ] Show sync status di UI

**6. Implement Firestore Real-time Listeners** ⏱️ 1 day
- [ ] Add Firestore listeners untuk user data
- [ ] Listen to daily logs changes
- [ ] Update UI in real-time
- [ ] Handle listener lifecycle (start/stop dengan app state)

**7. Update All ViewModels with Backend** ⏱️ 2 days
- [ ] Update `HomeViewModel`:
  - [ ] Fetch daily logs dari backend
  - [ ] Load statistics dari API
  - [ ] Real-time updates
- [ ] Update `FoodViewModel`:
  - [ ] Search foods via API
  - [ ] Log meals ke backend
  - [ ] Load history dari API
- [ ] Update `TipsViewModel`:
  - [ ] Fetch tips dari backend
  - [ ] Load personalized recommendations
- [ ] Update `ProfileViewModel`:
  - [ ] Update profile via API
  - [ ] Sync goals dengan backend

**8. End-to-End Testing** ⏱️ 1 day
- [ ] Test complete user flow:
  - [ ] Register → Onboarding → Home
  - [ ] Scan food → Log meal → View stats
  - [ ] Edit profile → Update goals
- [ ] Test offline mode
- [ ] Test sync after network restoration
- [ ] Fix bugs

### Sprint 5 Deliverables:
✅ Firebase Authentication working end-to-end
✅ All API endpoints integrated
✅ Offline-first architecture implemented
✅ Data sync working (Room ↔ Firestore)
✅ Food detection connected to backend
✅ Real-time updates working
✅ Full user flow functional

---

# FASE 4: ADVANCED FEATURES

## 🏃 Sprint 6: Analytics & Visualizations
**Duration:** 2 weeks
**Goal:** Enhanced charts, insights, export features

### Week 1: Charts & Analytics

**1. Implement Advanced Charts** ⏱️ 3 days
- [ ] Install charting library:
  ```kotlin
  implementation "com.github.PhilJay:MPAndroidChart:v3.1.0"
  ```
- [ ] Create `AnalyticsScreen.kt`:
  - [ ] Weekly calorie chart (line chart)
  - [ ] Macro distribution (pie chart)
  - [ ] Nutrient trends (multi-line chart)
  - [ ] Weight progress (line chart)
- [ ] Create `AnalyticsViewModel.kt`
- [ ] Fetch data dari stats API
- [ ] Add interactive chart controls (zoom, pan)
- [ ] Add date range selector

**2. Implement Insights System** ⏱️ 2 days
- [ ] Create `InsightsCard` component
- [ ] Create insight types:
  - [ ] Calorie deficit/surplus alerts
  - [ ] Nutrient deficiency warnings
  - [ ] Streak achievements
  - [ ] Goal progress updates
- [ ] Backend: Create insights generation service
- [ ] Display insights di HomeScreen
- [ ] Add insight notifications

### Week 2: Export & Sharing

**3. Implement Data Export** ⏱️ 2 days
- [ ] Create export functionality:
  - [ ] Export to CSV
  - [ ] Export to PDF report
  - [ ] Export date range selection
- [ ] Backend endpoint `GET /api/v1/export`:
  - [ ] Generate CSV dengan meal logs
  - [ ] Generate PDF dengan charts & summary
- [ ] Add share functionality (Share API)

**4. Implement Meal History Search** ⏱️ 1 day
- [ ] Create `MealHistoryScreen.kt`
- [ ] Add search bar dengan filters:
  - [ ] Search by food name
  - [ ] Filter by meal type
  - [ ] Filter by date range
- [ ] Add sorting options
- [ ] Pagination untuk large datasets

**5. Implement Weight Tracking** ⏱️ 2 days
- [ ] Add weight entry di ProfileScreen
- [ ] Create weight history chart
- [ ] Backend: Weight tracking endpoints
- [ ] Calculate weight change trends
- [ ] Set weight goals

### Sprint 6 Deliverables:
✅ Advanced charts implemented
✅ Analytics screen complete
✅ Insights system working
✅ Data export (CSV/PDF) functional
✅ Meal history with search
✅ Weight tracking feature

---

## 🏃 Sprint 7-8: Enhanced Features & Polish
**Duration:** 4 weeks
**Goal:** Meal planning, barcode database, notifications, UI polish

### Sprint 7: Meal Planning & Barcode Database

**1. Implement Meal Planning** ⏱️ 1 week
- [ ] Create `MealPlanScreen.kt`
- [ ] Weekly meal plan view
- [ ] Drag-and-drop meal scheduling
- [ ] Recipe suggestions based on goals
- [ ] Shopping list generation
- [ ] Backend: Meal plan endpoints

**2. Expand Barcode Database** ⏱️ 1 week
- [ ] Integrate Open Food Facts API
- [ ] Create barcode scanning improvement flow
- [ ] Allow users to submit new barcodes
- [ ] Admin moderation system
- [ ] Cache popular products locally

### Sprint 8: Notifications & Polish

**3. Implement Push Notifications** ⏱️ 1 week
- [ ] Setup Firebase Cloud Messaging:
  ```kotlin
  implementation 'com.google.firebase:firebase-messaging-ktx'
  ```
- [ ] Backend: Cloud Functions untuk scheduled notifications
- [ ] Notification types:
  - [ ] Daily log reminders
  - [ ] Meal time reminders
  - [ ] Goal achievement celebrations
  - [ ] Weekly progress reports
- [ ] Notification preferences di Settings

**4. UI/UX Polish** ⏱️ 1 week
- [ ] Add animations & transitions
- [ ] Improve loading states
- [ ] Add empty states dengan illustrations
- [ ] Implement pull-to-refresh
- [ ] Add haptic feedback
- [ ] Dark mode improvements
- [ ] Accessibility improvements (TalkBack, content descriptions)
- [ ] Error message improvements
- [ ] Onboarding tutorial overlay

### Sprint 7-8 Deliverables:
✅ Meal planning feature complete
✅ Barcode database expanded
✅ Push notifications working
✅ UI polished & refined
✅ Accessibility improved
✅ Dark mode perfected

---

# FASE 5: TESTING & DEPLOYMENT

## 🏃 Sprint 9: Comprehensive Testing
**Duration:** 2 weeks
**Goal:** Complete testing coverage, bug fixes

### Week 1: Unit & Integration Testing

**1. Android Unit Tests** ⏱️ 2 days
- [ ] ViewModel tests dengan JUnit & MockK:
  ```kotlin
  testImplementation "junit:junit:4.13.2"
  testImplementation "io.mockk:mockk:1.13.8"
  testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0"
  testImplementation "app.cash.turbine:turbine:1.0.0"
  ```
- [ ] Repository tests
- [ ] UseCase tests
- [ ] Utility function tests
- [ ] Target: 80% code coverage

**2. Android UI Tests** ⏱️ 2 days
- [ ] Compose UI tests:
  ```kotlin
  androidTestImplementation "androidx.compose.ui:ui-test-junit4"
  debugImplementation "androidx.compose.ui:ui-test-manifest"
  ```
- [ ] Screen tests (login, register, home, etc.)
- [ ] Navigation tests
- [ ] User flow tests (end-to-end)

**3. Backend Unit Tests** ⏱️ 1 day
- [ ] Service layer tests
- [ ] Repository tests
- [ ] Utility tests
- [ ] Mock Firestore untuk testing
- [ ] Target: 80% coverage

**4. Backend Integration Tests** ⏱️ 1 day
- [ ] API endpoint tests
- [ ] Authentication tests
- [ ] Database integration tests
- [ ] External API mock tests

### Week 2: Load Testing & Bug Fixes

**5. Load & Performance Testing** ⏱️ 2 days
- [ ] Backend load testing dengan k6:
  - [ ] Test concurrent users
  - [ ] Test API response times
  - [ ] Test database performance
- [ ] Android performance testing:
  - [ ] Profile app dengan Android Profiler
  - [ ] Identify memory leaks
  - [ ] Optimize slow screens
- [ ] Optimize based on results

**6. Security Testing** ⏱️ 1 day
- [ ] Firestore security rules testing
- [ ] API authentication testing
- [ ] Input validation testing
- [ ] XSS/SQL injection prevention
- [ ] Secure credential storage

**7. Bug Fixing** ⏱️ 2 days
- [ ] Fix all critical bugs
- [ ] Fix high-priority bugs
- [ ] Address test failures
- [ ] Code cleanup & refactoring

**8. Documentation** ⏱️ 1 day
- [ ] Update README.md
- [ ] API documentation review
- [ ] Code comments review
- [ ] Create deployment guide
- [ ] Create user manual

### Sprint 9 Deliverables:
✅ 80%+ test coverage (Android & Backend)
✅ All UI flows tested
✅ Performance optimized
✅ Security verified
✅ Critical bugs fixed
✅ Documentation complete

---

## 🏃 Sprint 10: Deployment & Launch
**Duration:** 2 weeks
**Goal:** Production deployment, beta testing, launch

### Week 1: Backend Deployment

**1. Prepare Backend for Production** ⏱️ 1 day
- [ ] Environment configuration (prod, staging, dev)
- [ ] Create `Dockerfile`:
  ```dockerfile
  FROM golang:1.21-alpine AS builder
  WORKDIR /app
  COPY . .
  RUN go build -o main cmd/api/main.go

  FROM alpine:latest
  RUN apk --no-cache add ca-certificates
  WORKDIR /root/
  COPY --from=builder /app/main .
  EXPOSE 8080
  CMD ["./main"]
  ```
- [ ] Create `.dockerignore`
- [ ] Test Docker build locally

**2. Deploy to Google Cloud Run** ⏱️ 1 day
- [ ] Create Google Cloud project
- [ ] Enable Cloud Run API
- [ ] Create Cloud Build config:
  ```yaml
  steps:
    - name: 'gcr.io/cloud-builders/docker'
      args: ['build', '-t', 'gcr.io/$PROJECT_ID/nutritrack-api', '.']
    - name: 'gcr.io/cloud-builders/docker'
      args: ['push', 'gcr.io/$PROJECT_ID/nutritrack-api']
    - name: 'gcr.io/cloud-builders/gcloud'
      args:
        - 'run'
        - 'deploy'
        - 'nutritrack-api'
        - '--image=gcr.io/$PROJECT_ID/nutritrack-api'
        - '--region=asia-southeast1'
        - '--platform=managed'
        - '--allow-unauthenticated'
  ```
- [ ] Deploy dengan Cloud Build
- [ ] Configure custom domain (optional)
- [ ] Setup SSL certificate

**3. Setup CI/CD Pipeline** ⏱️ 1 day
- [ ] Create GitHub Actions workflow:
  ```yaml
  name: Deploy Backend
  on:
    push:
      branches: [main]
  jobs:
    deploy:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - name: Setup Cloud SDK
          uses: google-github-actions/setup-gcloud@v1
        - name: Deploy to Cloud Run
          run: gcloud builds submit --config cloudbuild.yaml
  ```
- [ ] Setup staging environment
- [ ] Automated testing before deploy

**4. Setup Monitoring & Logging** ⏱️ 1 day
- [ ] Enable Cloud Logging
- [ ] Enable Cloud Monitoring
- [ ] Create custom dashboards
- [ ] Setup error alerting
- [ ] Configure uptime checks

### Week 2: Android Deployment & Launch

**5. Prepare Android App for Release** ⏱️ 2 days
- [ ] Update `build.gradle.kts`:
  - [ ] Version code & name
  - [ ] Enable ProGuard/R8:
    ```kotlin
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    ```
- [ ] Configure signing config
- [ ] Generate release APK/AAB
- [ ] Test release build thoroughly
- [ ] Update production API URLs

**6. Beta Testing** ⏱️ 2 days
- [ ] Create Google Play Console account
- [ ] Create app listing
- [ ] Upload AAB to Internal Testing track
- [ ] Invite beta testers (5-10 users)
- [ ] Collect feedback
- [ ] Fix critical issues

**7. Play Store Listing** ⏱️ 2 days
- [ ] Create app screenshots (phone & tablet)
- [ ] Create feature graphic
- [ ] Write app description:
  - [ ] Short description (80 chars)
  - [ ] Full description
  - [ ] Highlight key features
- [ ] Create promotional video (optional)
- [ ] Select category & tags
- [ ] Set up pricing (free)
- [ ] Privacy policy URL
- [ ] Content rating questionnaire

**8. Production Launch** ⏱️ 1 day
- [ ] Final testing on production backend
- [ ] Upload to Production track (staged rollout 10% → 50% → 100%)
- [ ] Monitor crash reports via Firebase Crashlytics:
  ```kotlin
  implementation 'com.google.firebase:firebase-crashlytics-ktx'
  ```
- [ ] Monitor user reviews
- [ ] Prepare hotfix process

### Sprint 10 Deliverables:
✅ Backend deployed to Cloud Run
✅ CI/CD pipeline working
✅ Monitoring & logging configured
✅ Android release build signed
✅ Beta testing complete
✅ Play Store listing live
✅ App launched to production
✅ Post-launch monitoring active

---

## 📈 Post-Launch Roadmap

### Maintenance Phase (Ongoing)

**Monthly Tasks:**
- [ ] Monitor crash reports & fix bugs
- [ ] Review user feedback & ratings
- [ ] Update food database
- [ ] Security updates
- [ ] Performance optimization
- [ ] Firebase quota monitoring

**Quarterly Updates:**
- [ ] Add new features based on feedback
- [ ] UI/UX improvements
- [ ] Expand food database
- [ ] Optimize backend costs

### Future Features (Backlog)

**Social Features:**
- [ ] Friend system
- [ ] Share meals & achievements
- [ ] Community challenges
- [ ] Leaderboards

**Advanced Nutrition:**
- [ ] Micronutrient tracking (vitamins, minerals)
- [ ] Water intake tracking
- [ ] Meal timing analysis
- [ ] Integration dengan fitness trackers (Google Fit)

**AI/ML Enhancements:**
- [ ] Custom food detection model (TensorFlow Lite)
- [ ] Personalized meal recommendations
- [ ] Predictive analytics
- [ ] Chatbot nutrition assistant

**Monetization (Optional):**
- [ ] Premium subscription tier
- [ ] Ad-free experience
- [ ] Advanced analytics
- [ ] Meal plan templates
- [ ] Personal coaching

---

## 🛠️ Technical Debt & Refactoring

**Priority Refactoring Tasks:**
1. Migrate to Kotlin Multiplatform (future iOS support)
2. Implement offline-first architecture fully
3. Add comprehensive error handling
4. Improve accessibility (WCAG compliance)
5. Reduce APK size
6. Optimize backend costs (caching, query optimization)

---

## 📊 Success Metrics (KPIs)

**User Metrics:**
- Daily Active Users (DAU)
- Monthly Active Users (MAU)
- User retention (D1, D7, D30)
- Average session duration
- Food logging frequency

**Technical Metrics:**
- App crash rate (< 1%)
- API response time (< 500ms p95)
- Backend uptime (> 99.9%)
- App store rating (> 4.0)

**Business Metrics:**
- User acquisition cost
- User lifetime value
- App store ranking
- Review sentiment analysis

---

## 🎯 Team & Resources

**Recommended Team:**
- 1 Android Developer (Kotlin/Compose)
- 1 Backend Developer (Golang)
- 1 UI/UX Designer (part-time)
- 1 QA Engineer (part-time)
- 1 DevOps Engineer (part-time)

**Estimated Total Timeline:** 20-24 weeks (5-6 months)

**Budget Considerations:**
- Firebase (Spark/Blaze plan): $0-50/month initially
- Google Cloud Run: ~$20-100/month
- Cloud Storage: ~$10-30/month
- Cloud Vision API: Pay per use
- Google Play Console: $25 one-time fee
- Domain (optional): ~$12/year
- Total: ~$50-200/month + initial $25

---

## ✅ Sprint Checklist Template

Use this for each sprint:

```markdown
## Sprint X: [Name]
**Date:** [Start] - [End]
**Goal:** [Sprint Goal]

### Planning
- [ ] Sprint planning meeting done
- [ ] Tasks estimated & assigned
- [ ] Definition of Done agreed

### Daily Standups
- [ ] Day 1 standup
- [ ] Day 2 standup
- [ ] ...

### Review
- [ ] Demo prepared
- [ ] Stakeholder feedback collected
- [ ] Retrospective done

### Deliverables
- [ ] All acceptance criteria met
- [ ] Code reviewed
- [ ] Tests passing
- [ ] Documentation updated
```

---

## 📝 Notes & Assumptions

1. **This plan assumes:**
   - 1-2 developers working full-time
   - Basic Kotlin & Golang knowledge
   - Access to Google Cloud & Firebase
   - Agile/Scrum methodology

2. **Risk Mitigation:**
   - Start with MVP, add features iteratively
   - Keep scope flexible for each sprint
   - Buffer time for unexpected issues
   - Regular testing prevents major bugs

3. **Success Factors:**
   - Clear requirements before coding
   - Regular code reviews
   - Continuous testing
   - User feedback integration
   - Good documentation

---

**Last Updated:** 2025-12-14
**Version:** 1.0
**Status:** Ready for Execution

---

**Next Step:** Choose Sprint 1 and start implementation! 🚀
