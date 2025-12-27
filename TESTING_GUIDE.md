h't h# 🚀 Quick Start: Testing the Authentication Flow

## 📱 Testing Checklist

### ✅ Prerequisites
- [ ] Backend updated with enhanced Google Sign-In response
- [ ] Android app compiled with latest changes
- [ ] Test Google account available
- [ ] Test regular account credentials

---

## 🧪 Test Scenarios

### 1️⃣ New User Registration (Regular)

**Steps:**
1. Open app
2. Tap "Create account"
3. Fill in registration form:
   - First Name: Test
   - Last Name: User
   - Gender: Male/Female
   - Birth Date: Select date
   - Email: testuser@example.com
   - Password: Test123!
4. Tap "Sign Up"

**Expected Result:**
```
✅ Account created
✅ Auto-login successful
✅ Navigate to Preferences screen
```

**Logs to Check:**
```bash
adb logcat | findstr "RegisterScreen\|LoginScreen"
```

Expected:
```
RegisterScreen: ✅ Register réussi, login automatique...
LoginScreen: ✅ Login successful
LoginScreen: → New user: Navigate to PREFERENCES
```

---

### 2️⃣ Complete Preferences

**Steps:**
1. On Preferences screen
2. Select Activity Level (e.g., "Beginner")
3. Answer Cycling questions
4. Answer Hiking questions
5. Answer Camping questions
6. Tap "Complete Onboarding"

**Expected Result:**
```
✅ Preferences saved
✅ Navigate to Home screen (Explore tab)
```

**Logs to Check:**
```bash
adb logcat | findstr "PreferencesAPI"
```

Expected:
```
PreferencesAPI: Sending request for userId: 676d...
PreferencesAPI: Success: OnboardingPreferencesResponse(...)
```

---

### 3️⃣ Logout & Login (Returning User)

**Steps:**
1. On Home screen, navigate to Profile
2. Tap Settings → Logout
3. Return to Login screen
4. Enter same credentials:
   - Email: testuser@example.com
   - Password: Test123!
5. Tap "Log in"

**Expected Result:**
```
✅ Login successful
✅ Skip Preferences screen
✅ Navigate directly to Home screen
```

**Logs to Check:**
```bash
adb logcat | findstr "LoginScreen\|checkPreferences"
```

Expected:
```
LoginScreen: 🔍 Regular login successful, checking preferences...
LoginViewModel: ✅ Preferences check complete: true
LoginScreen: → User has preferences: Navigate to HOME
```

---

### 4️⃣ First-Time Google Sign-In

**Steps:**
1. On Login screen
2. Tap Google Sign-In button
3. Select Google account
4. Authorize app

**Expected Result:**
```
✅ Google authentication successful
✅ Backend creates new account
✅ Navigate to Preferences screen
```

**Logs to Check:**
```bash
adb logcat | findstr "Google"
```

Expected:
```
LoginScreen: Calling viewModel.googleSignIn()
AuthRepository: ✅ Google Sign-In successful!
AuthRepository: 👤 User ID: 676d...
AuthRepository: 🆕 Is new user: true
LoginScreen: 🔵 Google Sign-In detected
LoginScreen: 🆕 Is new user: true
LoginScreen: → New Google user: Navigate to PREFERENCES
```

---

### 5️⃣ Complete Preferences (Google User)

**Steps:**
1. Complete preferences as in Test #2
2. Tap "Complete Onboarding"

**Expected Result:**
```
✅ Preferences saved
✅ Navigate to Home screen
```

---

### 6️⃣ Logout & Google Sign-In Again (Returning)

**Steps:**
1. Logout from Profile
2. Tap Google Sign-In button again
3. Select same Google account

**Expected Result:**
```
✅ Google authentication successful
✅ Backend recognizes existing account
✅ Skip Preferences screen
✅ Navigate directly to Home screen
```

**Logs to Check:**
```bash
adb logcat | findstr "Google"
```

Expected:
```
LoginScreen: Calling viewModel.googleSignIn()
AuthRepository: ✅ Google Sign-In successful!
AuthRepository: 👤 User ID: 676d...
AuthRepository: 🆕 Is new user: false
LoginScreen: 🔵 Google Sign-In detected
LoginScreen: 🆕 Is new user: false
LoginScreen: → Returning Google user: Navigate to HOME
```

---

### 7️⃣ App Restart (Existing Session)

**Steps:**
1. Force close app (don't logout)
2. Reopen app
3. Wait on Splash screen

**Expected Result:**
```
✅ Token validated
✅ Preferences checked from backend
✅ Navigate to Home screen (skip login)
```

**Logs to Check:**
```bash
adb logcat | findstr "SplashScreen"
```

Expected:
```
SplashScreen: 🔍 User logged in, checking preferences status...
SplashScreen: ✅ User has completed preferences → Home
```

---

## 🐛 Troubleshooting

### Issue: Always goes to Preferences

**Check:**
1. Backend returns `isNewUser: false` for returning Google users
2. Preferences were actually saved in database
3. Backend endpoint `/preferences/{userId}` returns correct data

**Debug:**
```bash
adb logcat | findstr "isNewUser\|needsPreferences"
```

### Issue: Google Sign-In fails

**Check:**
1. Google client ID configured in `google-services.json`
2. SHA-1 fingerprint registered in Google Console
3. Google Sign-In enabled in Firebase

**Debug:**
```bash
adb logcat | findstr "Google\|ApiException"
```

### Issue: Regular login doesn't check preferences

**Check:**
1. UserId saved in SharedPreferences after login
2. JWT contains userId claim
3. Token saved correctly

**Debug:**
```bash
adb logcat | findstr "userId\|auth_token"
```

---

## 📊 Test Matrix

| Scenario | Login Type | User Status | Expected Navigation |
|----------|-----------|-------------|---------------------|
| 1 | Register | New | Login → Preferences → Home |
| 2 | Regular | Returning (with prefs) | Login → Home |
| 3 | Regular | Returning (no prefs) | Login → Preferences |
| 4 | Google | New | Login → Preferences → Home |
| 5 | Google | Returning | Login → Home |
| 6 | Auto | Existing session | Splash → Home |

---

## 🔍 Log Monitoring Commands

### Windows PowerShell

**All authentication logs:**
```powershell
adb logcat | Select-String "LoginScreen|RegisterScreen|SplashScreen|LoginViewModel|AuthRepository"
```

**Google-specific:**
```powershell
adb logcat | Select-String "Google|isNewUser"
```

**Preference checking:**
```powershell
adb logcat | Select-String "preferences|needsPreferences|onboarding"
```

**Navigation events:**
```powershell
adb logcat | Select-String "Navigate|Navigation"
```

### Clear Logcat Before Test
```powershell
adb logcat -c
```

---

## 📸 Screenshots to Verify

### Expected Flow (New User)

1. **Register Screen**
   - Fill form fields
   
2. **Preferences Screen** (after signup)
   - Activity level selection
   - Multiple preference categories

3. **Home Screen** (after preferences)
   - Explore tab visible
   - Adventures listed

### Expected Flow (Returning User)

1. **Login Screen**
   - Email/password or Google button

2. **Home Screen** (direct, skip preferences)
   - Immediate access to explore

---

## ✅ Success Criteria

All tests should pass with:
- ✅ No crashes
- ✅ Correct navigation for each scenario
- ✅ Preferences shown only when needed
- ✅ Google sign-in distinguishes new vs returning users
- ✅ Regular login checks backend for preferences
- ✅ Splash screen handles existing sessions

---

## 🎯 Quick Test (5 Minutes)

**Minimal test to verify everything works:**

1. **Register new account** → Should go to Preferences
2. **Complete preferences** → Should go to Home
3. **Logout**
4. **Login with same credentials** → Should go to Home (skip preferences)
5. **Logout**
6. **Google sign-in (first time)** → Should go to Preferences
7. **Complete preferences**
8. **Logout**
9. **Google sign-in (second time)** → Should go to Home (skip preferences)

If all 9 steps work correctly, implementation is successful! ✅

---

## 📞 Need Help?

1. Check `AUTHENTICATION_FLOW_GUIDE.md` for detailed flow explanation
2. Check `BACKEND_UPDATE_REQUIRED.md` for backend changes
3. Monitor logs with specific filters
4. Verify backend responses match expected format

**Last Updated:** December 26, 2025

