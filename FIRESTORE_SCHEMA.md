# Firestore Database Schema - NutriTrack

## Collections Overview

```
nutritrack-db/
├── users/
│   └── {userId}/
├── meals/
│   └── {mealId}/
├── daily_logs/
│   └── {dailyLogId}/
└── user_saved_foods/        ← NEW!
    └── {savedFoodId}/
```

---

## 1. **users** Collection

Stores user profile and nutrition goals.

**Document ID**: Firebase Auth UID

```json
{
  "userId": "string",
  "email": "string",
  "displayName": "string",
  "photoUrl": "string",
  "age": 25,
  "gender": "male",
  "height": 170.0,
  "weight": 70.0,
  "activityLevel": "moderate",
  "targetCalories": 2000,
  "targetProtein": 150,
  "targetCarbs": 250,
  "targetFat": 67,
  "createdAt": 1734567890,
  "updatedAt": 1734567890
}
```

---

## 2. **meals** Collection

Stores individual meal entries.

**Document ID**: Auto-generated

```json
{
  "id": "meal_abc123",
  "userId": "user_xyz",
  "foodId": "food_001",
  "foodName": "Nasi Putih",
  "date": "2025-12-23",
  "mealType": "lunch",
  "portion": 1.5,
  "calories": 293,
  "protein": 6.0,
  "carbs": 64.5,
  "fat": 0.45,
  "createdAt": 1734567890,
  "updatedAt": 1734567890
}
```

**Indexes**:
- `userId` + `date` (composite)
- `userId` + `mealType` (composite)

---

## 3. **daily_logs** Collection

Aggregated daily nutrition summary (auto-calculated by backend).

**Document ID**: `{userId}_{date}` (e.g., `user123_2025-12-23`)

```json
{
  "id": "user123_2025-12-23",
  "userId": "user123",
  "date": "2025-12-23",
  "totalCalories": 1850.0,
  "totalProtein": 125.5,
  "totalCarbs": 220.0,
  "totalFat": 55.3,
  "mealsCount": 4,
  "updatedAt": 1734567890
}
```

**Indexes**:
- `userId` + `date` (composite)

---

## 4. **user_saved_foods** Collection ⭐ NEW

Stores user's frequently eaten foods with custom portions.

**Document ID**: Auto-generated

```json
{
  "id": "saved_abc123",
  "userId": "user_xyz",
  "foodId": "food_001",
  "foodName": "Nasi Putih",
  "note": "My regular portion (1.5 serving)",
  "customPortion": 1.5,
  "calories": 293,
  "protein": 6.0,
  "carbs": 64.5,
  "fat": 0.45,
  "servingSize": "150 g",
  "category": "grains",
  "createdAt": 1734567890,
  "lastUsedAt": 1734567890,
  "useCount": 15
}
```

**Purpose**:
- Quick access to frequently eaten foods
- Custom portion sizes (e.g., user always eats 1.5 servings of rice)
- Track usage frequency for smart suggestions
- Personal notes (e.g., "Breakfast size", "Gym day portion")

**Indexes**:
- `userId` + `lastUsedAt` (composite, descending)
- `userId` + `useCount` (composite, descending)
- `userId` + `category` (composite)

**Use Cases**:
1. **Quick Add**: User can quickly log saved foods without searching
2. **Smart Suggestions**: Show most frequently used foods first
3. **Custom Portions**: Save custom portion sizes (e.g., "My bowl of nasi goreng")
4. **Meal Templates**: Create templates for common meals

---

## Security Rules (Firestore Rules)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Users can only read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    match /meals/{mealId} {
      allow read, write: if request.auth != null &&
                           resource.data.userId == request.auth.uid;
    }

    match /daily_logs/{logId} {
      allow read: if request.auth != null &&
                    resource.data.userId == request.auth.uid;
      allow write: if false; // Only backend can write
    }

    // NEW: User saved foods
    match /user_saved_foods/{savedFoodId} {
      allow read, create: if request.auth != null &&
                            request.resource.data.userId == request.auth.uid;
      allow update, delete: if request.auth != null &&
                              resource.data.userId == request.auth.uid;
    }
  }
}
```

---

## API Endpoints Related to Collections

### Users
- `GET /api/v1/users/me` - Get current user
- `PUT /api/v1/users/me` - Update profile
- `PUT /api/v1/users/me/goals` - Update nutrition goals

### Meals
- `POST /api/v1/meals` - Create meal
- `GET /api/v1/meals/:id` - Get meal
- `PUT /api/v1/meals/:id` - Update meal
- `DELETE /api/v1/meals/:id` - Delete meal

### Daily Logs
- `GET /api/v1/daily-logs/:date` - Get daily summary
- `GET /api/v1/daily-logs?start_date=&end_date=` - Get range

### Foods (System Database)
- `GET /api/v1/foods?q=nasi` - Search foods
- `GET /api/v1/foods/:id` - Get food details
- `GET /api/v1/foods/barcode/:barcode` - Scan barcode

### User Saved Foods (Android only - no backend API yet)
- Managed via `UserSavedFoodRepository` in Android
- Direct Firestore access with security rules

---

## Collection Sizes (Estimated)

| Collection | Docs per User | Total (10K users) |
|------------|---------------|-------------------|
| users | 1 | 10,000 |
| meals | ~90/month | 900K/month |
| daily_logs | ~30/month | 300K/month |
| user_saved_foods | ~10-50 | 100K-500K |

**Total Storage**: ~1.3M documents for 10K active users/month
