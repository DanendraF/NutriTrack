# NutriTrack Backend API

Backend REST API untuk aplikasi NutriTrack yang dibangun dengan Go dan Firebase.

## Tech Stack

- **Go 1.21+** - Programming language
- **Gin** - Web framework
- **Firebase Admin SDK** - Authentication & Firestore database
- **godotenv** - Environment variable management

## Project Structure

```
nutritrack-backend/
├── cmd/
│   └── api/
│       └── main.go              # Entry point aplikasi
├── internal/
│   ├── config/
│   │   └── config.go            # Configuration management
│   ├── handlers/
│   │   ├── ping.go              # Health check handler
│   │   ├── user.go              # User CRUD handlers
│   │   ├── meal.go              # Meal CRUD handlers
│   │   ├── daily_log.go         # Daily log handlers
│   │   └── food.go              # Food database handlers
│   ├── middleware/
│   │   ├── cors.go              # CORS middleware
│   │   ├── logger.go            # Request logging middleware
│   │   ├── recovery.go          # Panic recovery middleware
│   │   └── auth.go              # Firebase authentication middleware
│   ├── models/                  # Data models (TODO)
│   └── services/                # Business logic (TODO)
├── pkg/
│   └── firebase/
│       └── firebase.go          # Firebase initialization
├── .env                         # Environment variables (gitignored)
├── .env.example                 # Environment template
├── .gitignore
├── go.mod
├── go.sum
└── README.md
```

## Prerequisites

1. **Go 1.21 atau lebih baru**
   ```bash
   go version
   ```

2. **Firebase Project**
   - Buat project di [Firebase Console](https://console.firebase.google.com)
   - Enable Firestore Database
   - Enable Authentication

3. **Service Account Key**
   - Di Firebase Console, buka **Project Settings** → **Service Accounts**
   - Klik **Generate New Private Key**
   - Download file JSON dan simpan sebagai `serviceAccountKey.json` di root folder backend
   - **PENTING:** File ini jangan di-commit ke Git!

## Installation

1. **Clone repository** (jika belum)
   ```bash
   cd nutritrack-backend
   ```

2. **Install dependencies**
   ```bash
   go mod download
   ```

3. **Setup environment variables**
   ```bash
   # Copy template .env
   cp .env.example .env
   ```

4. **Edit .env file**
   ```bash
   ENVIRONMENT=development
   PORT=8080
   FIREBASE_CREDENTIALS_PATH=serviceAccountKey.json
   ALLOWED_ORIGIN=*
   ```

5. **Download Firebase Service Account Key**
   - Letakkan file `serviceAccountKey.json` di root folder backend

## Running the Server

### Development Mode
```bash
go run cmd/api/main.go
```

Server akan berjalan di `http://localhost:8080`

### Build Binary
```bash
# Build untuk Windows
go build -o bin/api.exe cmd/api/main.go

# Build untuk Linux
GOOS=linux GOARCH=amd64 go build -o bin/api cmd/api/main.go

# Jalankan binary
./bin/api
```

## API Endpoints

### Public Endpoints

| Method | Endpoint       | Description        |
|--------|----------------|--------------------|
| GET    | /health        | Health check       |
| GET    | /api/v1/ping   | Simple ping test   |

### Protected Endpoints (Requires Firebase Auth)

**User Management**
| Method | Endpoint           | Description              |
|--------|--------------------|--------------------------|
| GET    | /api/v1/users/me   | Get current user profile |
| PUT    | /api/v1/users/me   | Update user profile      |

**Meal Management**
| Method | Endpoint              | Description          |
|--------|----------------------|----------------------|
| GET    | /api/v1/meals        | Get all user meals   |
| POST   | /api/v1/meals        | Create new meal      |
| GET    | /api/v1/meals/:id    | Get meal by ID       |
| PUT    | /api/v1/meals/:id    | Update meal          |
| DELETE | /api/v1/meals/:id    | Delete meal          |

**Daily Logs**
| Method | Endpoint                   | Description              |
|--------|---------------------------|--------------------------|
| GET    | /api/v1/daily-logs        | Get user daily logs      |
| GET    | /api/v1/daily-logs/:date  | Get daily log by date    |

**Food Database**
| Method | Endpoint             | Description           |
|--------|---------------------|-----------------------|
| GET    | /api/v1/foods       | Search foods          |
| GET    | /api/v1/foods/:id   | Get food by ID        |

## Authentication

Semua protected endpoints memerlukan Firebase ID Token di header:

```bash
Authorization: Bearer <FIREBASE_ID_TOKEN>
```

### Cara mendapatkan ID Token dari Android App:
```kotlin
val user = FirebaseAuth.getInstance().currentUser
user?.getIdToken(false)?.addOnSuccessListener { result ->
    val idToken = result.token
    // Gunakan idToken untuk API request
}
```

## Testing the API

### Quick Test dengan Scripts

**Windows (PowerShell):**
```powershell
# Test health
.\test.ps1 health

# Search foods
.\test.ps1 search nasi

# Run full test suite
.\test.ps1 full
```

**Linux/Mac (Bash):**
```bash
# Make executable
chmod +x test.sh

# Test health
./test.sh health

# Search foods
./test.sh search ayam

# Run full test suite
./test.sh full
```

### Manual Testing dengan cURL

#### 1. Health Check (Public)
```bash
curl http://localhost:8080/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "nutritrack-backend",
  "version": "1.0.0",
  "time": "2025-12-20T12:00:00Z"
}
```

#### 2. Search Foods (Protected)
```bash
# Set your Firebase ID token
TOKEN="your_firebase_id_token"

# Search for "nasi"
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/v1/foods?q=nasi"
```

**Response:**
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
        "fat": 0.3
      },
      "servingSize": {"amount": 100, "unit": "g"}
    }
  ],
  "total": 2,
  "limit": 20,
  "offset": 0,
  "hasMore": false
}
```

#### 3. Create User Profile
```bash
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

**📖 For detailed testing guide, see [TESTING.md](TESTING.md)**

## Environment Variables

| Variable                   | Description                          | Default              |
|---------------------------|--------------------------------------|----------------------|
| ENVIRONMENT               | Environment mode                     | development          |
| PORT                      | Server port                          | 8080                 |
| FIREBASE_CREDENTIALS_PATH | Path to Firebase service account key | serviceAccountKey.json |
| ALLOWED_ORIGIN            | CORS allowed origins                 | *                    |

## Development Workflow

1. **Start server**
   ```bash
   go run cmd/api/main.go
   ```

2. **Make changes** ke code

3. **Restart server** (Go tidak auto-reload, bisa gunakan [air](https://github.com/cosmtrek/air) untuk hot reload)

4. **Test endpoints** menggunakan curl atau Postman

## Next Steps (TODO)

- [ ] Implement Firestore operations di handlers
- [ ] Create request/response models di `internal/models/`
- [ ] Create business logic di `internal/services/`
- [ ] Add input validation
- [ ] Add unit tests
- [ ] Add integration tests
- [ ] Setup CI/CD pipeline
- [ ] Add rate limiting
- [ ] Add request ID tracking
- [ ] Implement pagination for list endpoints
- [ ] Add proper error responses

## Troubleshooting

### Error: "Failed to initialize Firebase"
- Pastikan file `serviceAccountKey.json` ada di root folder
- Pastikan `FIREBASE_CREDENTIALS_PATH` di .env sudah benar
- Pastikan file JSON valid dan dari Firebase Console

### Error: "Port already in use"
- Ubah PORT di .env ke port lain (misal 8081)
- Atau matikan aplikasi yang menggunakan port 8080

### Error: "Module not found"
- Jalankan `go mod download` untuk install dependencies
- Pastikan `go.mod` dan `go.sum` ada

## License

MIT
