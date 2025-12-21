# Testing NutriTrack Backend API

Dokumentasi lengkap untuk testing REST API backend NutriTrack menggunakan berbagai tools.

---

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Start the Server](#start-the-server)
3. [Testing dengan cURL](#testing-dengan-curl)
4. [Testing dengan Postman](#testing-dengan-postman)
5. [Testing dengan Go Test](#testing-dengan-go-test)
6. [Firebase Authentication Flow](#firebase-authentication-flow)
7. [Common Test Scenarios](#common-test-scenarios)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required:
- ✅ Go 1.21+ installed
- ✅ Firebase project setup
- ✅ `serviceAccountKey.json` downloaded
- ✅ `.env` file configured

### Optional Tools:
- **cURL** (built-in Windows/Linux/Mac)
- **Postman** (download: https://www.postman.com/)
- **HTTPie** (optional, prettier output)
- **Insomnia** (alternative to Postman)

---

## Start the Server

### Option 1: Run with Go
```bash
cd nutritrack-backend
go run cmd/api/main.go
```

### Option 2: Run compiled binary
```bash
# Build first
go build -o bin/api.exe cmd/api/main.go

# Run
./bin/api.exe
```

### Expected Output:
```
🔧 Starting NutriTrack Backend...
📝 Environment: development
📝 Port: 8080
🔥 Initializing Firebase Admin SDK...
✅ Firebase initialized successfully
📦 Initializing repositories...
✅ Repositories initialized
🌱 Checking food database...
✅ Food database already seeded with 29 items
🔧 Initializing handlers...
✅ Handlers initialized
🚀 Server starting on port 8080...
```

Server akan running di: **http://localhost:8080**

---

## Testing dengan cURL

### 1. Health Check (Public Endpoint)

```bash
curl http://localhost:8080/health
```

**Expected Response:**
```json
{
  "status": "healthy",
  "service": "nutritrack-backend",
  "version": "1.0.0",
  "time": "2025-12-20T12:00:00+07:00"
}
```

### 2. Ping Test (Public Endpoint)

```bash
curl http://localhost:8080/api/v1/ping
```

**Expected Response:**
```json
{
  "message": "pong",
  "timestamp": "2025-12-20T12:00:00+07:00"
}
```

---

## Testing Protected Endpoints (Require Auth)

**IMPORTANT:** Protected endpoints require Firebase ID Token in Authorization header.

### Get Firebase ID Token

Ada 2 cara:

#### Option A: Dari Android App (Production Way)
```kotlin
// In Android app
val user = FirebaseAuth.getInstance().currentUser
user?.getIdToken(false)?.addOnSuccessListener { result ->
    val idToken = result.token
    Log.d("Token", idToken)
    // Use this token for API requests
}
```

#### Option B: Dari Firebase Console (Testing Only)
1. Buka Firebase Console → Authentication
2. Create test user dengan email/password
3. Gunakan Firebase REST Auth API untuk login:

```bash
# Replace with your Firebase Web API Key (dari Firebase Console → Project Settings)
API_KEY="YOUR_FIREBASE_WEB_API_KEY"

# Login to get ID token
curl -X POST "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "test123",
    "returnSecureToken": true
  }'
```

**Response akan include:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6...",
  "email": "test@example.com",
  "refreshToken": "...",
  "expiresIn": "3600",
  "localId": "abc123..."
}
```

**Simpan `idToken` dan `localId` untuk testing.**

---

## Testing User Endpoints

### 1. Create User Profile

```bash
TOKEN="YOUR_FIREBASE_ID_TOKEN"

curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "name": "John Doe",
    "dateOfBirth": "1995-05-15",
    "gender": "male",
    "height": 175,
    "weight": 70
  }'
```

**Expected Response:**
```json
{
  "id": "abc123...",
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

### 2. Get Current User

```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Update User Profile

```bash
curl -X PUT http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "weight": 72
  }'
```

### 4. Update Nutrition Goals

```bash
curl -X PUT http://localhost:8080/api/v1/users/me/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "activityLevel": "active",
    "nutritionGoal": "lose"
  }'
```

**Note:** Server akan auto-recalculate BMR, TDEE, dan target macros!

### 5. Delete User Account

```bash
curl -X DELETE http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Testing Food Endpoints

### 1. Search All Foods

```bash
curl -X GET "http://localhost:8080/api/v1/foods" \
  -H "Authorization: Bearer $TOKEN"
```

### 2. Search Foods by Query

```bash
# Search "nasi"
curl -X GET "http://localhost:8080/api/v1/foods?q=nasi" \
  -H "Authorization: Bearer $TOKEN"

# Search "ayam"
curl -X GET "http://localhost:8080/api/v1/foods?q=ayam" \
  -H "Authorization: Bearer $TOKEN"

# Search dengan limit & offset (pagination)
curl -X GET "http://localhost:8080/api/v1/foods?q=telur&limit=10&offset=0" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "foods": [
    {
      "id": "uuid-here",
      "name": "Egg (Boiled)",
      "nameIndonesian": "Telur Rebus",
      "category": "protein",
      "nutrition": {
        "calories": 155,
        "protein": 13,
        "carbs": 1.1,
        "fat": 11,
        "fiber": 0,
        "sugar": 1.1,
        "sodium": 124
      },
      "servingSize": {
        "amount": 100,
        "unit": "g"
      },
      "barcode": "",
      "imageUrl": "",
      "isVerified": true,
      "source": "system",
      "createdAt": "2025-12-20T12:00:00Z"
    }
  ],
  "total": 1,
  "limit": 20,
  "offset": 0,
  "hasMore": false
}
```

### 3. Filter by Category

```bash
# Get all fruits
curl -X GET "http://localhost:8080/api/v1/foods?category=fruits" \
  -H "Authorization: Bearer $TOKEN"

# Get all protein sources
curl -X GET "http://localhost:8080/api/v1/foods?category=protein" \
  -H "Authorization: Bearer $TOKEN"

# Available categories: fruits, vegetables, grains, protein, dairy, snacks
```

### 4. Get Food by ID

```bash
FOOD_ID="uuid-from-search-results"

curl -X GET "http://localhost:8080/api/v1/foods/$FOOD_ID" \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Get Foods by Category (Alternative)

```bash
curl -X GET "http://localhost:8080/api/v1/foods/category/fruits" \
  -H "Authorization: Bearer $TOKEN"
```

### 6. Create New Food (Admin)

```bash
curl -X POST http://localhost:8080/api/v1/foods \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Banana Smoothie",
    "nameIndonesian": "Smoothie Pisang",
    "category": "snacks",
    "nutrition": {
      "calories": 150,
      "protein": 4,
      "carbs": 30,
      "fat": 2,
      "fiber": 3,
      "sugar": 20,
      "sodium": 50
    },
    "servingSize": {
      "amount": 250,
      "unit": "ml"
    }
  }'
```

---

## Testing dengan Postman

### 1. Import Collection

Create new Postman Collection: **NutriTrack API**

### 2. Setup Environment Variables

In Postman → Environments → Create New:

```
BASE_URL = http://localhost:8080
TOKEN = your_firebase_id_token_here
```

### 3. Create Requests

#### Request 1: Health Check
- **Method:** GET
- **URL:** `{{BASE_URL}}/health`
- **Headers:** None
- **Auth:** None

#### Request 2: Get Current User
- **Method:** GET
- **URL:** `{{BASE_URL}}/api/v1/users/me`
- **Headers:**
  - `Authorization: Bearer {{TOKEN}}`
- **Auth:** None (using manual header)

#### Request 3: Search Foods
- **Method:** GET
- **URL:** `{{BASE_URL}}/api/v1/foods`
- **Params:**
  - `q`: nasi
  - `category`: grains
  - `limit`: 10
- **Headers:**
  - `Authorization: Bearer {{TOKEN}}`

#### Request 4: Create User
- **Method:** POST
- **URL:** `{{BASE_URL}}/api/v1/users`
- **Headers:**
  - `Authorization: Bearer {{TOKEN}}`
  - `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "email": "test@example.com",
  "name": "Test User",
  "dateOfBirth": "1990-01-01",
  "gender": "male",
  "height": 170,
  "weight": 65
}
```

---

## Testing dengan Go Test

### Create Test File

`internal/handlers/user_handler_test.go`:

```go
package handlers_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/nutritrack/backend/internal/handlers"
	"github.com/nutritrack/backend/internal/models"
	"github.com/stretchr/testify/assert"
)

func TestHealthCheck(t *testing.T) {
	// Setup
	gin.SetMode(gin.TestMode)
	router := gin.Default()

	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"status": "healthy",
		})
	})

	// Test
	req, _ := http.NewRequest("GET", "/health", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	// Assert
	assert.Equal(t, http.StatusOK, w.Code)

	var response map[string]interface{}
	json.Unmarshal(w.Body.Bytes(), &response)
	assert.Equal(t, "healthy", response["status"])
}

func TestPing(t *testing.T) {
	gin.SetMode(gin.TestMode)
	router := gin.Default()
	router.GET("/ping", handlers.Ping)

	req, _ := http.NewRequest("GET", "/ping", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
}
```

### Run Tests

```bash
# Run all tests
go test ./...

# Run tests with verbose output
go test -v ./...

# Run specific test
go test -v ./internal/handlers -run TestHealthCheck

# Run tests with coverage
go test -cover ./...

# Generate coverage report
go test -coverprofile=coverage.out ./...
go tool cover -html=coverage.out
```

---

## Common Test Scenarios

### Scenario 1: Complete User Flow

```bash
TOKEN="your_token"

# 1. Create user profile
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "name": "New User",
    "dateOfBirth": "1995-06-20",
    "gender": "female",
    "height": 165,
    "weight": 58
  }'

# 2. Get user profile
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"

# 3. Update weight after 1 month
curl -X PUT http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"weight": 56}'

# 4. Change goal to lose weight
curl -X PUT http://localhost:8080/api/v1/users/me/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "activityLevel": "active",
    "nutritionGoal": "lose"
  }'
```

### Scenario 2: Food Discovery Flow

```bash
TOKEN="your_token"

# 1. Browse all foods
curl -X GET "http://localhost:8080/api/v1/foods?limit=50" \
  -H "Authorization: Bearer $TOKEN"

# 2. Search for breakfast foods
curl -X GET "http://localhost:8080/api/v1/foods?q=telur" \
  -H "Authorization: Bearer $TOKEN"

# 3. Get protein foods
curl -X GET "http://localhost:8080/api/v1/foods/category/protein" \
  -H "Authorization: Bearer $TOKEN"

# 4. Get specific food details
curl -X GET "http://localhost:8080/api/v1/foods/FOOD_ID_HERE" \
  -H "Authorization: Bearer $TOKEN"
```

### Scenario 3: Error Testing

```bash
# Test without auth token (should fail)
curl -X GET http://localhost:8080/api/v1/users/me

# Test with invalid token (should fail)
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer invalid_token_here"

# Test create user with missing fields (should fail)
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Incomplete User"
  }'
```

---

## Testing Database Seeding

### Check Seeded Foods

Server auto-seeds 29 Indonesian foods on first startup. Verify:

```bash
TOKEN="your_token"

# Count total foods
curl -X GET "http://localhost:8080/api/v1/foods" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.total'

# List all categories
curl -X GET "http://localhost:8080/api/v1/foods?limit=100" \
  -H "Authorization: Bearer $TOKEN" \
  | jq '.foods[].category' | sort | uniq

# Expected categories:
# - fruits (6 items)
# - vegetables (5 items)
# - protein (6 items)
# - dairy (3 items)
# - grains (6 items)
# - snacks (3 items)
```

---

## Performance Testing

### Simple Load Test with cURL

```bash
# Send 10 concurrent requests
for i in {1..10}; do
  curl -X GET http://localhost:8080/health &
done
wait
```

### Load Testing with Apache Bench

```bash
# Install Apache Bench (ab)
# Ubuntu: sudo apt install apache2-utils
# Mac: brew install httpie

# Test health endpoint (100 requests, 10 concurrent)
ab -n 100 -c 10 http://localhost:8080/health

# Test with auth (save token in file)
ab -n 100 -c 10 -H "Authorization: Bearer $(cat token.txt)" \
  http://localhost:8080/api/v1/foods
```

---

## Troubleshooting

### Issue 1: "Authorization header required"

**Problem:** Missing Firebase ID token

**Solution:**
```bash
# Make sure to include Authorization header
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Issue 2: "User not found"

**Problem:** User profile not created yet

**Solution:**
```bash
# Create user profile first
curl -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...user data...}'
```

### Issue 3: Port 8080 already in use

**Problem:** Another process using port 8080

**Solution:**
```bash
# Windows
netstat -ano | findstr :8080
taskkill //F //PID <PID>

# Linux/Mac
lsof -i :8080
kill -9 <PID>

# Or change port in .env
PORT=8081
```

### Issue 4: Firebase initialization failed

**Problem:** serviceAccountKey.json not found

**Solution:**
1. Download service account key from Firebase Console
2. Save as `serviceAccountKey.json` in backend root
3. Verify path in `.env`: `FIREBASE_CREDENTIALS_PATH=serviceAccountKey.json`

---

## Quick Reference

### Categories Available:
- `fruits` - Buah-buahan
- `vegetables` - Sayuran
- `protein` - Protein (daging, telur, tahu, tempe)
- `dairy` - Susu & produk susu
- `grains` - Karbohidrat (nasi, roti, kentang)
- `snacks` - Camilan

### Sample Foods Seeded:
- **Fruits:** Pisang, Apel, Mangga, Pepaya, Semangka, Jeruk
- **Vegetables:** Bayam, Wortel, Tomat, Kubis, Buncis
- **Protein:** Dada Ayam, Telur Rebus, Tahu, Tempe, Ikan Nila, Daging Sapi
- **Dairy:** Susu Low Fat, Yogurt, Keju Cheddar
- **Grains:** Nasi Putih, Nasi Merah, Roti Gandum, Oatmeal, Kentang, Ubi
- **Snacks:** Kacang Tanah, Singkong Rebus, Jagung Rebus

---

## Next Steps

1. ✅ Test all endpoints manually
2. ✅ Verify database seeding
3. ✅ Create test user & test CRUD
4. 🔨 Integrate with Android app
5. 🔨 Add more foods to database
6. 🔨 Implement meal logging endpoints

---

**Happy Testing! 🚀**
