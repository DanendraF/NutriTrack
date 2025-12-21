# Network Troubleshooting - Android to Backend Connection

## Problem: "Failed to connect to 10.0.2.2:8080 after 30000ms"

### Root Cause
- Emulator tidak bisa reach `10.0.2.2` (localhost alias)
- Network policy atau firewall blocking

### Solutions

---

## Solution 1: Test Connectivity dari Emulator (Recommended First)

**Test apakah 10.0.2.2 reachable:**

1. Di emulator, buka **Chrome browser**
2. Navigate ke: `http://10.0.2.2:8080/health`
3. Harusnya muncul JSON response:
   ```json
   {"service":"nutritrack-backend","status":"healthy"...}
   ```

**Jika TIDAK bisa:**
- Emulator issue → Restart emulator
- Firewall blocking → Allow port 8080
- Lanjut ke Solution 2

**Jika BISA:**
- Problem bukan di network
- Check Logcat untuk error lain

---

## Solution 2: Ganti ke IP Address Komputer

### Step 1: Cari IP Address Komputer

**Windows CMD:**
```cmd
ipconfig
```

Look for WiFi adapter atau Ethernet adapter:
```
IPv4 Address. . . . . . . . . . . : 192.168.0.239
```

**Biasanya format:**
- `192.168.x.x` (WiFi rumah)
- `10.x.x.x` (Corporate network)

### Step 2: Update Base URL di KoinModule.kt

**File:** `app/src/main/java/com/example/nutritrack/di/KoinModule.kt`

**Before:**
```kotlin
single {
    Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/") // Android emulator localhost
        .client(get())
        .addConverterFactory(GsonConverterFactory.create(get()))
        .build()
}
```

**After (ganti dengan IP komputer):**
```kotlin
single {
    Retrofit.Builder()
        .baseUrl("http://192.168.0.239:8080/") // ← Ganti dengan IP komputer!
        .client(get())
        .addConverterFactory(GsonConverterFactory.create(get()))
        .build()
}
```

### Step 3: Update Network Security Config

**File:** `app/src/main/res/xml/network_security_config.xml`

**Add IP komputer:**
```xml
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
        <!-- Add IP komputer -->
        <domain includeSubdomains="true">192.168.0.239</domain>
    </domain-config>

    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
```

### Step 4: Rebuild & Test

```bash
cd NutriTrack
./gradlew --stop
./gradlew compileDebugKotlin
```

Then run app di Android Studio

---

## Solution 3: Test dengan Localhost Alternative

Jika pakai **physical device** (bukan emulator), ganti ke:
- `http://192.168.0.239:8080/` (IP komputer di network yang sama)

**JANGAN pakai:**
- `http://localhost:8080/` ❌ (ini localhost device, bukan host)
- `http://127.0.0.1:8080/` ❌ (sama dengan localhost)

---

## Solution 4: Check Firewall

**Windows Firewall bisa block port 8080:**

1. Windows Security → Firewall & network protection
2. Advanced settings
3. Inbound Rules → New Rule
4. Port → TCP → Specific local ports: **8080**
5. Allow the connection
6. Apply

**Atau disable Firewall sementara (for testing):**
```cmd
# Run as Administrator
netsh advfirewall set allprofiles state off
```

**Re-enable setelah testing:**
```cmd
netsh advfirewall set allprofiles state on
```

---

## Verification Checklist

**Backend:**
- [ ] Backend running: `curl http://localhost:8080/health`
- [ ] Port listening: `netstat -ano | findstr :8080`
- [ ] Firewall allows port 8080

**Android:**
- [ ] Internet permission in Manifest
- [ ] Network security config correct
- [ ] Base URL correct (10.0.2.2 atau IP komputer)
- [ ] Emulator/device di network yang sama (jika pakai IP komputer)

**Test dari emulator:**
- [ ] Chrome browser bisa buka `http://10.0.2.2:8080/health`
- [ ] Atau buka `http://[IP_KOMPUTER]:8080/health`

---

## Quick Test Script

**PowerShell (from host machine):**
```powershell
# Test localhost
curl.exe http://localhost:8080/health

# Test dari IP komputer (simulate external access)
curl.exe http://192.168.0.239:8080/health
```

**Expected response:**
```json
{"service":"nutritrack-backend","status":"healthy","time":"...","version":"1.0.0"}
```

---

## Common Errors & Fixes

### Error: "Connection refused"
- Backend not running
- Wrong port
- **Fix:** Start backend: `go run cmd/api/main.go`

### Error: "Connection timeout"
- Firewall blocking
- Wrong IP address
- **Fix:** Check firewall, verify IP correct

### Error: "Network security policy"
- Cleartext traffic blocked
- **Fix:** Already fixed in network_security_config.xml

### Error: "Unable to resolve host"
- DNS issue
- **Fix:** Use IP address instead of hostname

---

## Current Configuration

**Your computer IPs:**
```
10.2.0.2
10.5.0.2
172.31.128.1
192.168.56.1
192.168.239.2
192.168.0.239  ← Recommended (WiFi LAN)
```

**Backend:**
- Running on: `localhost:8080` (all interfaces)
- Health check: `http://localhost:8080/health`

**Android App:**
- Current base URL: `http://10.0.2.2:8080/`
- Alternative: `http://192.168.0.239:8080/` (if 10.0.2.2 not working)

---

## Testing Steps

1. **Verify backend running:**
   ```bash
   curl http://localhost:8080/health
   ```

2. **Test from emulator browser:**
   - Open Chrome in emulator
   - Go to `http://10.0.2.2:8080/health`
   - If works → Check Logcat for other errors
   - If fails → Use IP address (Solution 2)

3. **If using IP address, test reachability:**
   ```bash
   curl http://192.168.0.239:8080/health
   ```

4. **Update code & rebuild**

5. **Monitor Logcat:**
   ```
   Filter: OkHttp
   ```
   Look for connection errors

---

**Next:** Try Solution 1 first, then Solution 2 if needed.
