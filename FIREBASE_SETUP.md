# Firebase Setup Instructions

## Mendapatkan Service Account Key

Backend NutriTrack memerlukan Firebase Service Account Key untuk autentikasi dengan Firebase Admin SDK.

### Langkah-langkah:

1. **Buka Firebase Console**
   - Akses: https://console.firebase.google.com
   - Pilih project: **NutriTrack** (atau nama project Anda)

2. **Navigate ke Service Accounts**
   - Klik ⚙️ **Project Settings** (icon gear di sidebar kiri)
   - Pilih tab **Service Accounts**

3. **Generate Private Key**
   - Scroll ke bawah sampai section "Firebase Admin SDK"
   - Klik tombol **Generate new private key**
   - Akan muncul dialog konfirmasi
   - Klik **Generate key**

4. **Download & Install**
   - File JSON akan otomatis terdownload
   - **Rename file** menjadi: `serviceAccountKey.json`
   - **Pindahkan file** ke folder: `nutritrack-backend/` (root folder backend, sejajar dengan README.md)

5. **Verify**
   ```bash
   # Check apakah file sudah ada
   ls -la serviceAccountKey.json
   ```

   File structure seharusnya:
   ```
   nutritrack-backend/
   ├── cmd/
   ├── internal/
   ├── pkg/
   ├── .env
   ├── .env.example
   ├── serviceAccountKey.json  ← File ini harus ada
   ├── go.mod
   └── README.md
   ```

## Security Warning ⚠️

**PENTING:**
- File `serviceAccountKey.json` berisi kredensial sensitif
- **JANGAN** commit file ini ke Git
- File ini sudah ada di `.gitignore`
- **JANGAN** share file ini ke orang lain
- Jika file ter-leak, segera **revoke** di Firebase Console

## Testing

Setelah file serviceAccountKey.json tersedia, test backend:

```bash
# Start server
go run cmd/api/main.go

# Expected output:
# 🚀 Server starting on port 8080...

# Test health endpoint
curl http://localhost:8080/health
```

## Troubleshooting

### Error: "Failed to initialize Firebase"
- Pastikan file `serviceAccountKey.json` ada di root folder backend
- Pastikan nama file **exact**: `serviceAccountKey.json` (case-sensitive)
- Pastikan file valid JSON (download ulang jika perlu)

### Error: "permission denied serviceAccountKey.json"
- Check file permissions
- Pastikan file readable oleh user yang menjalankan server

## Firebase Project Configuration

Pastikan Firebase project sudah enable:
- ✅ **Authentication** (Email/Password enabled)
- ✅ **Firestore Database** (Native mode)
- ✅ **Firebase Storage** (jika akan upload gambar)

Check di Firebase Console:
1. **Authentication** → Sign-in method → Email/Password → **Enabled**
2. **Firestore Database** → Create database → Start in **production mode** atau **test mode**
3. **Storage** → Get started (opsional, untuk foto profil/foto makanan)

## Next Steps After Setup

Setelah Firebase credentials tersedia:

1. ✅ Start backend server
2. ✅ Test health endpoint
3. ✅ Test authentication dengan Firebase ID token dari Android app
4. 🔨 Implement Firestore CRUD operations di handlers
5. 🔨 Connect Android app ke backend API
