# Firebase Setup Guide for NutriTrack

## 📋 Prerequisites
- Google Account
- Android Studio installed
- NutriTrack project ready

---

## 🔥 Step 1: Create Firebase Project

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Click "Add project" or "Create a project"

2. **Project Setup**
   - **Project name**: `NutriTrack` (or your preferred name)
   - Click "Continue"

3. **Google Analytics** (Optional)
   - Enable/Disable Google Analytics (recommended: Enable)
   - Select or create Analytics account
   - Click "Create project"

4. **Wait for project creation** (30-60 seconds)
   - Click "Continue" when ready

---

## 📱 Step 2: Add Android App to Firebase

1. **Register App**
   - In Firebase Console, click the Android icon
   - **Android package name**: `com.example.nutritrack`
     - ⚠️ Must match your `applicationId` in `app/build.gradle.kts`
   - **App nickname** (optional): `NutriTrack Android`
   - **Debug signing certificate SHA-1** (optional for now, needed later for Google Sign-In)

2. **Get SHA-1 Certificate** (Optional, but recommended)
   ```bash
   cd android
   ./gradlew signingReport
   ```
   - Look for `SHA1:` under `Variant: debug`
   - Copy and paste into Firebase console

3. **Click "Register app"**

---

## 📥 Step 3: Download google-services.json

1. **Download Configuration File**
   - Click "Download google-services.json"
   - **IMPORTANT**: Save this file to:
     ```
     NutriTrack/app/google-services.json
     ```
   - The file should be in the `app/` directory, NOT in `app/src/`

2. **Verify File Location**
   ```
   NutriTrack/
   ├── app/
   │   ├── google-services.json  ← HERE
   │   ├── build.gradle.kts
   │   └── src/
   ```

3. **Click "Next"**

---

## ⚙️ Step 4: Add Firebase SDK (Already Done!)

✅ The Firebase SDK dependencies are already added to your project:

**build.gradle.kts** (project-level):
```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}
```

**app/build.gradle.kts**:
```kotlin
apply(plugin = "com.google.gms.google-services")

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

---

## 🔐 Step 5: Enable Firebase Authentication

1. **In Firebase Console**:
   - Go to **Build** → **Authentication**
   - Click "Get started"

2. **Enable Sign-in Methods**:

   **Email/Password:**
   - Click on "Email/Password"
   - Toggle "Enable"
   - Click "Save"

   **Google Sign-In (Optional):**
   - Click on "Google"
   - Toggle "Enable"
   - Select support email
   - Click "Save"

---

## 🗄️ Step 6: Create Firestore Database

1. **In Firebase Console**:
   - Go to **Build** → **Firestore Database**
   - Click "Create database"

2. **Security Rules** (Choose one):

   **Option A: Test Mode** (for development)
   - Start in test mode
   - ⚠️ Warning: Data will be public for 30 days
   - Good for initial development

   **Option B: Production Mode** (recommended)
   - Start in production mode
   - We'll add custom rules later

3. **Choose Location**:
   - Select closest region (e.g., `asia-southeast1` for Indonesia)
   - Click "Enable"

4. **Wait for database creation** (1-2 minutes)

---

## 🔒 Step 7: Configure Firestore Security Rules

1. **Go to** Firestore Database → **Rules** tab

2. **Replace with these rules**:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }

    // Helper function to check if user owns the document
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }

    // Users collection
    match /users/{userId} {
      // Allow read/write only if user is authenticated and owns the document
      allow read, write: if isOwner(userId);
    }

    // Daily logs collection
    match /daily_logs/{userId}/logs/{logId} {
      allow read, write: if isOwner(userId);
    }

    // Foods collection - read-only for all authenticated users
    match /foods/{foodId} {
      allow read: if isAuthenticated();
      allow write: if false; // Only admins can write (implement admin check later)
    }

    // Scanned images collection
    match /scanned_images/{userId}/images/{imageId} {
      allow read, write: if isOwner(userId);
    }
  }
}
```

3. **Click "Publish"**

---

## 📦 Step 8: Setup Cloud Storage (Optional, for images)

1. **In Firebase Console**:
   - Go to **Build** → **Storage**
   - Click "Get started"

2. **Security Rules**:
   - Choose "Start in test mode" (for now)
   - Click "Next"

3. **Storage Location**:
   - Select same region as Firestore
   - Click "Done"

4. **Update Storage Rules**:
   ```javascript
   rules_version = '2';
   service firebase.storage {
     match /b/{bucket}/o {
       match /users/{userId}/{allPaths=**} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
     }
   }
   ```

---

## ✅ Step 9: Verify Setup

1. **Sync Gradle**:
   - Open Android Studio
   - Click "Sync Now" when prompted
   - Or: File → Sync Project with Gradle Files

2. **Check for Errors**:
   - Build → Clean Project
   - Build → Rebuild Project

3. **Run the App**:
   - Should compile without Firebase errors
   - Firebase will auto-initialize on app start

---

## 🧪 Step 10: Test Firebase Connection

Add this code to `MainActivity.onCreate()` temporarily to test:

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log

// Inside onCreate(), after setContent:
val auth = Firebase.auth
val db = Firebase.firestore

Log.d("Firebase", "Auth instance: $auth")
Log.d("Firebase", "Firestore instance: $db")
```

Check Logcat for successful Firebase initialization.

---

## 📊 Firebase Collections Structure

### **users/{userId}**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "gender": "Male",
  "age": 25,
  "height": 175.0,
  "weight": 70.0,
  "activityLevel": "MODERATELY_ACTIVE",
  "nutritionGoal": "MAINTAIN_WEIGHT",
  "targetCalories": 2200,
  "targetProtein": 165.0,
  "targetCarbs": 247.5,
  "targetFat": 61.1,
  "createdAt": "2025-12-14T00:00:00Z",
  "updatedAt": "2025-12-14T00:00:00Z"
}
```

### **daily_logs/{userId}/logs/{date}**
```json
{
  "date": "2025-12-14",
  "meals": [
    {
      "mealId": "meal_123",
      "foodId": "food_456",
      "foodName": "Nasi Goreng",
      "mealType": "BREAKFAST",
      "quantity": 1.0,
      "portionUnit": "plate",
      "calories": 350.0,
      "protein": 12.0,
      "carbs": 55.0,
      "fat": 8.0,
      "timestamp": 1702540800000,
      "imageUrl": null
    }
  ],
  "summary": {
    "totalCalories": 1280.0,
    "totalProtein": 45.0,
    "totalCarbs": 150.0,
    "totalFat": 40.0
  },
  "mealCount": 3,
  "updatedAt": "2025-12-14T12:30:00Z"
}
```

### **foods/{foodId}**
```json
{
  "name": "Nasi Goreng",
  "nameIndonesian": "Nasi Goreng",
  "category": "GRAINS",
  "nutrition": {
    "calories": 350.0,
    "protein": 12.0,
    "carbs": 55.0,
    "fat": 8.0,
    "fiber": 2.0,
    "sugar": 3.0,
    "sodium": 800.0
  },
  "servingSize": {
    "amount": 1.0,
    "unit": "plate"
  },
  "barcode": null,
  "imageUrl": null,
  "isVerified": true,
  "source": "local",
  "createdAt": "2025-12-01T00:00:00Z"
}
```

---

## 🔑 Important Notes

1. **google-services.json**:
   - ⚠️ NEVER commit this file to public repositories
   - Add to `.gitignore`
   - Each team member needs their own for development

2. **Security**:
   - Test mode rules expire after 30 days
   - Update to production rules before launch
   - Implement proper admin checks for write operations

3. **Regions**:
   - Choose region closest to your users
   - Can't change after creation
   - Affects latency and costs

4. **Costs**:
   - Firestore: Free tier = 50K reads, 20K writes, 20K deletes per day
   - Authentication: Free up to 10K phone auths per month
   - Storage: Free tier = 5GB stored, 1GB downloaded per day

---

## 🚀 Next Steps

After Firebase setup is complete:

1. ✅ Implement Firebase Authentication in AuthViewModel
2. ✅ Create FirebaseRepository for Firestore operations
3. ✅ Implement CRUD operations
4. ✅ Test end-to-end user flow
5. ✅ Add offline persistence
6. ✅ Implement data sync

---

## 🐛 Troubleshooting

### "google-services.json not found"
- Verify file is in `app/` directory
- File name must be exact: `google-services.json`
- Sync Gradle after adding file

### "Default FirebaseApp is not initialized"
- Check `google-services.json` is in correct location
- Verify `apply(plugin = "com.google.gms.google-services")` in build.gradle
- Clean and rebuild project

### "FirebaseApp initialization unsuccessful"
- Check `applicationId` matches Firebase console
- Verify internet connection
- Check Firebase Console for project status

### Build errors after adding Firebase
- Sync Gradle files
- Invalidate Caches and Restart Android Studio
- Clean build folder

---

**Setup Complete!** 🎉

You're now ready to implement Firebase Authentication and Firestore operations.

**Last Updated**: 2025-12-14
