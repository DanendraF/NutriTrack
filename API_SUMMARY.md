# NutriTrack Backend API - Quick Reference

**Version:** 1.0.0
**Base URL:** `http://localhost:8080`
**Auth:** Firebase ID Token (Bearer)

---

## 🚀 Quick Start

1. **Start Server:**
   ```bash
   go run cmd/api/main.go
   # or
   ./bin/api.exe
   ```

2. **Test Health:**
   ```bash
   curl http://localhost:8080/health
   ```

3. **Get Firebase Token:** (from Android app or Firebase Console)

4. **Test Protected Endpoint:**
   ```bash
   TOKEN="your_token_here"
   curl -H "Authorization: Bearer $TOKEN" \
        "http://localhost:8080/api/v1/foods?q=nasi"
   ```

---

## 📋 API Endpoints Summary

### Public Endpoints (No Auth Required)

| Method | Endpoint           | Description      |
|--------|--------------------|------------------|
| GET    | `/health`          | Health check     |
| GET    | `/api/v1/ping`     | Ping test        |

---

### User Management (🔒 Auth Required)

| Method | Endpoint                  | Body                                  | Response                    |
|--------|---------------------------|---------------------------------------|-----------------------------|
| POST   | `/api/v1/users`           | CreateUserRequest                     | UserResponse (201)          |
| GET    | `/api/v1/users/me`        | -                                     | UserResponse (200)          |
| PUT    | `/api/v1/users/me`        | UpdateUserRequest                     | UserResponse (200)          |
| PUT    | `/api/v1/users/me/goals`  | UpdateGoalsRequest                    | UserResponse (200)          |
| DELETE | `/api/v1/users/me`        | -                                     | Success message (200)       |

#### CreateUserRequest
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "dateOfBirth": "1995-05-15",
  "gender": "male",
  "height": 175,
  "weight": 70
}
```

#### UpdateUserRequest
```json
{
  "name": "John Updated",
  "weight": 72
}
```

#### UpdateGoalsRequest
```json
{
  "activityLevel": "active",
  "nutritionGoal": "lose"
}
```

**Activity Levels:** `sedentary`, `light`, `moderate`, `active`, `very_active`
**Nutrition Goals:** `lose`, `maintain`, `gain`

---

### Food Database (🔒 Auth Required)

| Method | Endpoint                        | Query Params                    | Response                    |
|--------|--------------------------------|--------------------------------|-----------------------------|
| GET    | `/api/v1/foods`                | q, category, limit, offset     | FoodSearchResponse (200)    |
| GET    | `/api/v1/foods/:id`            | -                              | FoodResponse (200)          |
| GET    | `/api/v1/foods/barcode/:code`  | -                              | FoodResponse (200)          |
| GET    | `/api/v1/foods/category/:cat`  | -                              | Foods array (200)           |
| POST   | `/api/v1/foods`                | CreateFoodRequest              | FoodResponse (201)          |
| PUT    | `/api/v1/foods/:id`            | CreateFoodRequest              | FoodResponse (200)          |
| DELETE | `/api/v1/foods/:id`            | -                              | Success message (200)       |

#### Query Parameters

- `q` - Search query (food name in English or Indonesian)
- `category` - Filter by category: `fruits`, `vegetables`, `protein`, `dairy`, `grains`, `snacks`
- `limit` - Max results per page (default: 20, max: 100)
- `offset` - Pagination offset (default: 0)

#### CreateFoodRequest
```json
{
  "name": "Banana",
  "nameIndonesian": "Pisang",
  "category": "fruits",
  "nutrition": {
    "calories": 89,
    "protein": 1.1,
    "carbs": 22.8,
    "fat": 0.3,
    "fiber": 2.6,
    "sugar": 12.2,
    "sodium": 1
  },
  "servingSize": {
    "amount": 100,
    "unit": "g"
  },
  "barcode": "123456789",
  "imageUrl": "https://..."
}
```

---

## 🍽️ Food Categories

| Category     | Indonesian       | Count | Examples                                |
|-------------|------------------|-------|-----------------------------------------|
| `fruits`    | Buah-buahan      | 6     | Pisang, Apel, Mangga, Pepaya           |
| `vegetables`| Sayuran          | 5     | Bayam, Wortel, Tomat, Kubis            |
| `protein`   | Protein          | 6     | Ayam, Telur, Tahu, Tempe, Ikan         |
| `dairy`     | Produk Susu      | 3     | Susu, Yogurt, Keju                     |
| `grains`    | Karbohidrat      | 6     | Nasi Putih, Nasi Merah, Roti, Kentang  |
| `snacks`    | Camilan          | 3     | Kacang, Singkong, Jagung               |

**Total Foods Seeded:** 29

---

## 📊 Response Examples

### UserResponse
```json
{
  "id": "abc123",
  "email": "user@example.com",
  "name": "John Doe",
  "dateOfBirth": "1995-05-15",
  "gender": "male",
  "measurements": {
    "height": 175,
    "weight": 70,
    "updatedAt": "2025-12-20T12:00:00Z"
  },
  "goals": {
    "activityLevel": "moderate",
    "nutritionGoal": "maintain",
    "targetCalories": 2500,
    "targetProtein": 187.5,
    "targetCarbs": 250,
    "targetFat": 83.33,
    "bmr": 1750.5,
    "tdee": 2500,
    "updatedAt": "2025-12-20T12:00:00Z"
  },
  "settings": {
    "units": "metric",
    "notifications": true,
    "theme": "system"
  },
  "createdAt": "2025-12-20T12:00:00Z",
  "updatedAt": "2025-12-20T12:00:00Z"
}
```

### FoodSearchResponse
```json
{
  "foods": [
    {
      "id": "uuid",
      "name": "White Rice (Cooked)",
      "nameIndonesian": "Nasi Putih",
      "category": "grains",
      "nutrition": {
        "calories": 130,
        "protein": 2.7,
        "carbs": 28.2,
        "fat": 0.3,
        "fiber": 0.4,
        "sugar": 0.1,
        "sodium": 1
      },
      "servingSize": {
        "amount": 100,
        "unit": "g"
      },
      "isVerified": true,
      "source": "system",
      "createdAt": "2025-12-20T12:00:00Z"
    }
  ],
  "total": 2,
  "limit": 20,
  "offset": 0,
  "hasMore": false
}
```

---

## 🔐 Authentication

All protected endpoints require Firebase ID Token:

```bash
# Header format
Authorization: Bearer <FIREBASE_ID_TOKEN>
```

### Get Token (Android)
```kotlin
FirebaseAuth.getInstance().currentUser?.getIdToken(false)
    ?.addOnSuccessListener { result ->
        val token = result.token
        // Use token for API calls
    }
```

### Get Token (cURL - Testing Only)
```bash
API_KEY="your_firebase_web_api_key"

curl -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "returnSecureToken": true
  }'
```

---

## ❌ Error Responses

| Status | Error                          | Cause                              |
|--------|--------------------------------|------------------------------------|
| 400    | Bad Request                    | Invalid request body/params        |
| 401    | Authorization header required  | Missing auth token                 |
| 401    | Invalid or expired token       | Invalid/expired Firebase token     |
| 404    | Not found                      | Resource doesn't exist             |
| 409    | User already exists            | Attempting to create existing user |
| 500    | Internal server error          | Server/database error              |

**Error Response Format:**
```json
{
  "error": "Error message here"
}
```

---

## 🧪 Testing Commands

### PowerShell (Windows)
```powershell
# Health check
.\test.ps1 health

# Create user
.\test.ps1 create-user

# Search foods
.\test.ps1 search "telur"

# Get category
.\test.ps1 category protein

# Count foods by category
.\test.ps1 count

# Full test suite
.\test.ps1 full
```

### Bash (Linux/Mac)
```bash
chmod +x test.sh

# Health check
./test.sh health

# Search foods
./test.sh search ayam

# Get category
./test.sh category fruits

# Full test suite
./test.sh full
```

### cURL Examples
```bash
TOKEN="your_token"

# Search "nasi"
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/v1/foods?q=nasi"

# Get protein foods
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/v1/foods/category/protein"

# Create user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "dateOfBirth": "1990-01-01",
    "gender": "male",
    "height": 170,
    "weight": 65
  }'

# Update user weight
curl -X PUT http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"weight": 68}'

# Update goals
curl -X PUT http://localhost:8080/api/v1/users/me/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "activityLevel": "active",
    "nutritionGoal": "lose"
  }'
```

---

## 📝 Notes

- Server auto-seeds 29 Indonesian foods on first startup
- BMR & TDEE are auto-calculated using Harris-Benedict equation
- Macros distribution: 40% carbs, 30% protein, 30% fat
- Nutrition goals: `lose` = -15% calories, `gain` = +15% calories, `maintain` = 0%
- All timestamps in ISO 8601 format (UTC)
- Pagination: Use `limit` and `offset` for large result sets

---

## 📚 Full Documentation

- **[README.md](README.md)** - Setup & installation guide
- **[TESTING.md](TESTING.md)** - Comprehensive testing guide
- **[FIREBASE_SETUP.md](FIREBASE_SETUP.md)** - Firebase configuration

---

**Last Updated:** 2025-12-20
**API Version:** 1.0.0
