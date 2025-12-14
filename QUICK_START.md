# NutriTrack - Quick Start Guide

## 🚀 LANGKAH CEPAT MULAI TESTING

### **Step 1: Fix Build Error** ⏱️ 5 menit

Build error yang muncul adalah Windows file lock issue (normal):

```bash
# 1. Close Android Studio SEPENUHNYA (tidak minimize, benar-benar close)
# Pastikan tidak ada proses Android Studio yang berjalan di Task Manager

# 2. Buka terminal/command prompt di folder project
cd "d:\UII\5\Pengembangan Aplikasi Bergerak\ProjectAkhir_Nutritrack\NutriTrack"

# 3. Run clean
./gradlew clean

# 4. Buka Android Studio lagi
# 5. Rebuild project (Build → Rebuild Project)
```

**Expected Result:** Build sukses tanpa error

---

### **Step 2: Setup Firebase Console** ⏱️ 30 menit

#### **2.1 Create Firebase Project**
1. Buka https://console.firebase.google.com
2. Klik "Add project" atau "Create a project"
3. Project name: **NutriTrack** (atau nama lain)
4. Enable Google Analytics (opsional)
5. Klik "Create project"

#### **2.2 Add Android App**
1. Di Firebase Console, klik ⚙️ (Settings) → Project Settings
2. Scroll ke "Your apps"
3. Klik icon Android (</>) untuk add Android app
4. **Android package name:** `com.example.nutritrack`
   - ⚠️ **HARUS SAMA** dengan package di `app/build.gradle.kts`
5. App nickname: NutriTrack (opsional)
6. **SKIP** download google-services.json (sudah ada)
7. Klik "Next" → "Continue to console"

#### **2.3 Enable Authentication**
1. Di sidebar Firebase Console, klik "Authentication"
2. Klik "Get started"
3. Tab "Sign-in method"
4. Klik "Email/Password"
5. **Enable** toggle pertama (Email/Password)
6. Klik "Save"

**Expected Result:** Email/Password provider aktif

#### **2.4 Create Firestore Database**
1. Di sidebar, klik "Firestore Database"
2. Klik "Create database"
3. **Location:** pilih asia-southeast1 (Jakarta) atau asia-southeast2 (Singapore)
4. **Security rules:** Pilih "Start in test mode" (sementara)
5. Klik "Create"

**Expected Result:** Database created successfully

#### **2.5 Configure Security Rules**
1. Di Firestore Database, tab "Rules"
2. Replace dengan rules ini:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only read/write their own document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Meals belong to users
    match /users/{userId}/meals/{mealId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Daily logs belong to users
    match /users/{userId}/dailyLogs/{logId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Foods are read-only for all authenticated users
    match /foods/{foodId} {
      allow read: if request.auth != null;
      allow write: if false; // Only admins can write (setup later)
    }
  }
}
```

3. Klik "Publish"

**Expected Result:** Rules published successfully

---

### **Step 3: Test Authentication Flow** ⏱️ 10 menit

#### **3.1 Run App**
```bash
# Pastikan emulator/device sudah running
./gradlew installDebug

# Atau run via Android Studio (Shift + F10)
```

#### **3.2 Register New User**
1. App terbuka di LoginScreen
2. Klik "Daftar di sini"
3. Input:
   - Username: `testuser`
   - Email: `test@nutritrack.com`
   - Password: `test123`
4. Klik "Daftar"

**Expected Result:**
- Loading indicator muncul
- Navigate ke WelcomeScreen (onboarding)

#### **3.3 Check Firebase Console**
1. Buka Firebase Console → Authentication → Users tab
2. **Should see:** Email `test@nutritrack.com` dengan Display Name `testuser`

**Expected Result:** User created successfully ✅

#### **3.4 Complete Onboarding**
1. **WelcomeScreen:** Klik "Let's Start"
2. **GenderAgeScreen:**
   - Name: `Test User`
   - Email: `test@nutritrack.com`
   - Gender: Pilih Male/Female
   - Klik "Next"
3. **MeasurementScreen:**
   - Tab Height: Drag ruler ke ~170 cm
   - Tab Weight: Drag ruler ke ~70 kg
   - Klik "Next"
4. **ActivityLevelScreen:**
   - Pilih "Moderately Active" (atau yang lain)
   - Klik "Next"
5. **NutritionGoalScreen:**
   - Pilih "Maintain Weight" (atau yang lain)
   - Klik "Next"
6. **CalculationResultScreen:**
   - Lihat hasil kalkulasi
   - Target Calories: ~2000-2500 (tergantung input)
   - Protein: ~150g
   - Carbs: ~250g
   - Fats: ~60g
   - Klik "Start Your Journey"

**Expected Result:**
- Navigate ke HomeScreen
- Data user tampil di UI

#### **3.5 Check Firestore Database**
1. Buka Firebase Console → Firestore Database
2. Collection: `users`
3. Document: (Firebase UID panjang)
4. **Should see data:**
   ```
   name: "Test User"
   email: "test@nutritrack.com"
   gender: "male" / "female"
   age: 25 (contoh)
   height: 170
   weight: 70
   activityLevel: "moderatelyactive"
   goal: "maintainweight"
   targetCalories: 2300 (contoh)
   targetProtein: 150
   targetCarbs: 250
   targetFats: 60
   createdAt: (timestamp)
   ```

**Expected Result:** User document created ✅

---

### **Step 4: Test Login Flow** ⏱️ 5 menit

#### **4.1 Logout (Manual)**
Karena logout button belum ada di UI:
- Close app completely
- Clear app data (Settings → Apps → NutriTrack → Clear data)
- Atau uninstall & reinstall

#### **4.2 Login**
1. App terbuka di LoginScreen
2. Input:
   - Email: `test@nutritrack.com`
   - Password: `test123`
3. Klik "Masuk"

**Expected Result:**
- Loading indicator
- Navigate langsung ke HomeScreen (skip onboarding)
- Data user tampil

---

### **Step 5: Verify Home Screen Data** ⏱️ 2 menit

Di HomeScreen, pastikan tampil:

**Top Bar:**
- ✅ Greeting: "Hi, Test User!"
- ✅ Target Calories: 2300 cal (contoh)
- ✅ Progress: 0%
- ✅ Sync icon (bisa diklik)

**Calories Card:**
- ✅ Consumed: 0 cal
- ✅ Target: 2300 cal
- ✅ Remaining: 2300 cal
- ✅ Progress bar: 0%

**Macros Cards:**
- ✅ Protein: 0g / 150g
- ✅ Carbs: 0g / 250g
- ✅ Fats: 0g / 60g

**Expected Result:** Semua data tampil correctly ✅

---

## ✅ CHECKLIST TESTING

### **Firebase Setup:**
- [ ] Firebase project created
- [ ] Android app added (package: com.example.nutritrack)
- [ ] Email/Password authentication enabled
- [ ] Firestore database created
- [ ] Security rules configured

### **App Testing:**
- [ ] Build berhasil tanpa error
- [ ] Register user baru berhasil
- [ ] User muncul di Firebase Auth Console
- [ ] Onboarding flow complete (6 screens)
- [ ] Nutrition calculation correct
- [ ] Data tersimpan di Firestore
- [ ] Home screen displays user data
- [ ] Login flow works
- [ ] Auto-login after restart

---

## 🐛 TROUBLESHOOTING

### **Build Error: "Couldn't delete R.jar"**
**Solution:** Close IDE → `./gradlew clean` → Reopen IDE

### **Firebase Auth Error: "Network error"**
**Check:**
- Internet connection
- Firebase project created
- google-services.json in `app/` folder
- Package name matches (`com.example.nutritrack`)

### **Firestore Permission Denied**
**Check:**
- Security rules published
- User authenticated (logged in)
- Document path correct: `users/{userId}`

### **Home Screen Shows Empty Data**
**Check:**
- User completed onboarding
- Data saved successfully (check Firestore Console)
- ViewModel loading data (check Logcat)

### **Navigation Doesn't Work**
**Check:**
- `MainActivity.kt` navigation setup
- Auth state check in MainActivity
- Navigation routes correct

---

## 📱 TESTING SCENARIOS

### **Scenario 1: New User Registration**
```
1. Open app → LoginScreen
2. Click "Daftar di sini"
3. Fill form → Submit
4. Navigate to Onboarding
5. Complete all screens
6. Navigate to Home
7. Data displayed correctly
```

### **Scenario 2: Existing User Login**
```
1. Open app → LoginScreen
2. Enter credentials
3. Navigate to Home (skip onboarding)
4. Data loaded from Firestore
```

### **Scenario 3: Offline Mode**
```
1. Login while online
2. Turn off internet
3. Navigate around app
4. Data still available (Room cache)
5. Turn on internet
6. Refresh → Sync dari Firestore
```

### **Scenario 4: Form Validation**
```
1. LoginScreen:
   - Empty email → Error
   - Invalid email → Error
   - Short password → Error
2. RegisterScreen:
   - Short username → Error
   - Invalid email → Error
   - Weak password → Error
```

---

## 📊 EXPECTED DATA FLOW

```
User Action → UI Event → ViewModel → Repository → Firebase/Room

Example: Register
┌─────────────────┐
│ RegisterScreen  │ User clicks "Daftar"
└────────┬────────┘
         ↓
┌─────────────────┐
│ AuthViewModel   │ validateFields() → register()
└────────┬────────┘
         ↓
┌─────────────────┐
│ AuthRepository  │ createUserWithEmailAndPassword()
└────────┬────────┘
         ↓
┌─────────────────┐
│ Firebase Auth   │ Creates user account
└────────┬────────┘
         ↓
┌─────────────────┐
│ Success State   │ Navigate to Onboarding
└─────────────────┘

Example: Save Onboarding Data
┌─────────────────────┐
│ CalculationResult   │ User clicks "Start Journey"
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│ OnboardingViewModel │ calculateNutritionTargets() → saveUserData()
└──────────┬──────────┘
           ↓
┌─────────────────────┐
│ UserRepository      │ insertUser() + saveToFirestore()
└──────────┬──────────┘
           ↓
     ┌────┴────┐
     ↓         ↓
┌──────┐  ┌──────────┐
│ Room │  │ Firestore│
└──────┘  └──────────┘
  Local     Cloud
  Cache     Backup
```

---

## 🎯 SUCCESS CRITERIA

Setelah semua testing complete, Anda should have:

✅ **Firebase Setup:**
- Project created
- Authentication enabled
- Firestore database configured
- Security rules active

✅ **App Functionality:**
- User can register
- User can login
- Onboarding flow works
- Nutrition calculation accurate
- Data persists in Firestore
- Home screen displays data
- Offline mode works

✅ **Data Verification:**
- User document in Firestore
- Correct user data structure
- All fields populated
- Timestamps recorded

---

## 📝 NEXT DEVELOPMENT PHASE

After testing complete, next steps:

1. **Meal Logging** (Priority 1)
   - User can add meals
   - Meals saved to Firestore
   - Home screen shows consumed calories

2. **Food Database** (Priority 2)
   - Search food items
   - Select portions
   - Calculate nutrition

3. **Daily Log** (Priority 3)
   - Auto-calculate daily totals
   - History view
   - Weekly/monthly stats

4. **Profile & Settings** (Priority 4)
   - Edit user profile
   - Change goals
   - Logout functionality

---

**Created:** 2025-12-14
**Status:** Ready for testing! 🚀
**Next:** Complete Steps 1-5 above
