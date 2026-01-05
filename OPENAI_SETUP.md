# OpenAI GPT Integration Setup Guide

## 🔐 Setup API Key (PENTING!)

### 1. Dapatkan API Key Baru
1. Login ke https://platform.openai.com/api-keys
2. **REVOKE token lama yang sudah dibocorkan** (sk-proj-kTiYVqivznvCs2to2ghxxB4r993...)
3. Generate API key baru
4. Copy key tersebut (dimulai dengan `sk-proj-...`)

### 2. Tambahkan ke local.properties
Buka atau buat file `local.properties` di root project, tambahkan:

```properties
sdk.dir=C\:\\Users\\LENOVO\\AppData\\Local\\Android\\Sdk
OPENAI_API_KEY=sk-proj-YOUR-NEW-API-KEY-HERE
```

**PENTING**:
- Ganti `sk-proj-YOUR-NEW-API-KEY-HERE` dengan API key baru kamu
- File `local.properties` sudah di-gitignore, aman dari commit
- JANGAN PERNAH commit API key ke git!

### 3. Sync Gradle
1. Buka Android Studio
2. Klik "Sync Now" atau "Sync Project with Gradle Files"
3. Tunggu sampai sync selesai

## 🚀 Cara Kerja

### Fitur yang Diimplementasikan:
1. **GPT-powered Nutrition Calculation**
   - Menghitung kalori target berdasarkan profil user
   - Menghitung protein, carbs, fat yang optimal
   - Memberikan analisis dan tips personal dalam Bahasa Indonesia

### Flow:
```
User input onboarding data →
→ Call OpenAIService.calculateNutrition() →
→ GPT-3.5-turbo analyze & calculate →
→ Return personalized nutrition targets →
→ Save to user profile
```

### Fallback Strategy:
Jika GPT API gagal (error/timeout), sistem akan:
1. Log error
2. Gunakan default values (2000 kcal, dll)
3. User tetap bisa lanjut onboarding

## 📊 Cost Estimation

### GPT-3.5-turbo Pricing:
- Input: $0.0005 / 1K tokens
- Output: $0.0015 / 1K tokens
- Per calculation: ~500 tokens = $0.0007 (Rp 11)

### Dengan 1000 users:
- Total cost: $0.70 (Rp 11,000)
- Sangat affordable!

## 🔧 File yang Dimodifikasi:

1. **build.gradle.kts**
   - Added OpenAI client dependency
   - Added BuildConfig for API key

2. **OpenAIService.kt** (NEW)
   - Service untuk call GPT API
   - Parse response & fallback handling

3. **KoinModule.kt**
   - Register OpenAIService di DI

4. **.gitignore**
   - Protect .env and local.properties

## 🎯 Next Steps:

Setelah setup API key, kamu bisa:
1. Test calculation di onboarding
2. Monitor API usage di OpenAI dashboard
3. Adjust prompt jika perlu hasil yang berbeda
4. (Optional) Move to Cloud Functions untuk lebih secure

## ⚠️ Security Checklist:

- ✅ API key stored in local.properties (not in code)
- ✅ local.properties in .gitignore
- ✅ BuildConfig for secure access
- ✅ No hardcoded keys in repository
- ✅ Error handling & fallback implemented

## 🐛 Troubleshooting:

**Error: Unresolved reference OpenAIService**
- Solution: Sync Gradle, Clean & Rebuild project

**Error: OPENAI_API_KEY is empty**
- Solution: Check local.properties, add API key

**Error: API call timeout**
- Solution: Check internet connection, API key validity

**Error: Invalid API key**
- Solution: Regenerate new key from OpenAI dashboard
