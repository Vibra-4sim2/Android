# 🔧 CRITICAL FIX: UserId Not Being Extracted from JWT

## ❌ Problem Identified

From your logcat:
```
LoginScreen: 🔍 Login successful, checking preferences...
LoginScreen: 👤 UserId from JWT: null
LoginScreen: ⚠️ Cannot check preferences: userId or token missing
```

### Root Cause

The `saveAuthData()` function was saving to a different SharedPreferences file than what `UserPreferences.getUserId()` was reading from:

**Before (Broken):**
```kotlin
private fun saveAuthData(context: Context, accessToken: String) {
    val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val userId = extractUserIdFromToken(accessToken)
    with(sharedPref.edit()) {
        putString("access_token", "...") // Wrong location!
        putString("user_id", userId)
        apply()
    }
}
```

But `UserPreferences.getUserId()` uses:
```kotlin
fun getUserId(context: Context): String? {
    val token = getToken(context) // Gets from "user_prefs"
    return JwtHelper.getUserIdFromToken(token)
}
```

**Mismatch:** Saving to `auth_prefs` but reading from `user_prefs` via token!

---

## ✅ Solution Applied

**Fixed `saveAuthData()` to use UserPreferences:**

```kotlin
private fun saveAuthData(context: Context, accessToken: String) {
    // Save token using UserPreferences (to correct location)
    UserPreferences.saveToken(context, accessToken)
    
    // Extract and log userId for debugging
    val userId = com.example.dam.utils.JwtHelper.getUserIdFromToken(accessToken)
    Log.d("LoginScreen", "💾 Saving auth data - UserId: $userId")
    Log.d("LoginScreen", "💾 Token saved: ${accessToken.take(30)}...")
}
```

**Removed duplicate `extractUserIdFromToken()` function** - now uses the official `JwtHelper.getUserIdFromToken()`

---

## 🎯 What This Fixes

### Before Fix:
```
Login → Save token to "auth_prefs" 
     → getUserId() reads from "user_prefs" 
     → Returns NULL ❌
     → Can't check preferences
     → Stuck at login screen
```

### After Fix:
```
Login → Save token to "user_prefs" via UserPreferences
     → getUserId() reads from "user_prefs"
     → Extracts userId from JWT ✅
     → Checks preferences
     → Navigates to HOME or PREFERENCES correctly
```

---

## 🧪 Expected Behavior Now

### Test 1: Regular Login
```
1. Enter email/password
2. Click Login
3. Backend returns JWT with userId in "sub" claim
4. Token saved via UserPreferences.saveToken()
5. getUserId() extracts userId from saved token
6. Calls checkPreferencesStatus(userId, token)
7. Navigates based on preferences status
```

**Expected Logs:**
```
LoginScreen: 💾 Saving auth data - UserId: 691121ba31a13e25a7ca215d
LoginScreen: 💾 Token saved: eyJhbGciOiJIUzI1NiIsInR5cCI6Ik...
LoginScreen: 🔍 Login successful, checking preferences...
LoginScreen: 👤 UserId from JWT: 691121ba31a13e25a7ca215d
LoginViewModel: 🔍 Fetching preferences for userId: 691121ba31a13e25a7ca215d
```

### Test 2: Google Sign-In
```
1. Click Google Sign-In
2. Select account
3. Backend returns JWT
4. Token saved via UserPreferences
5. getUserId() works correctly
6. Preferences checked
7. Navigation works
```

---

## 📝 Files Changed

**File:** `LoginScreen.kt`

**Changes:**
1. ✅ Updated `saveAuthData()` to use `UserPreferences.saveToken()`
2. ✅ Removed duplicate `extractUserIdFromToken()` function
3. ✅ Now uses `JwtHelper.getUserIdFromToken()` for consistency
4. ✅ Added logging for debugging

---

## 🚀 Next Steps

### 1. Clean and Rebuild
```powershell
# Clean the project
.\gradlew clean

# Rebuild
.\gradlew assembleDebug
```

### 2. Uninstall Old App
```powershell
adb uninstall com.example.dam
```

### 3. Install Fresh
```powershell
.\gradlew installDebug
```

### 4. Test Login Flow
1. Open app
2. Go to Login
3. Enter credentials
4. Click Login

**Watch for these logs:**
```powershell
adb logcat | Select-String "💾 Saving auth data|👤 UserId from JWT"
```

**Should see:**
```
💾 Saving auth data - UserId: 691121ba31a13e25a7ca215d
👤 UserId from JWT: 691121ba31a13e25a7ca215d
```

**Not:**
```
👤 UserId from JWT: null  ❌
```

---

## 🔍 Verification Checklist

After installing the fix:

- [ ] Regular login extracts userId correctly
- [ ] Google sign-in extracts userId correctly  
- [ ] Preference checking works
- [ ] Navigation to HOME/PREFERENCES based on status
- [ ] No more "userId or token missing" errors

---

## 💡 Why This Happened

The code had **two different storage mechanisms**:
1. `saveAuthData()` - custom SharedPreferences
2. `UserPreferences` - centralized preference management

The fix consolidates everything to use **`UserPreferences`** which is the single source of truth.

---

## ✅ Status

**Fixed:** ✅ Complete  
**Tested:** Needs verification after rebuild  
**Impact:** Critical - enables entire login flow  

**This was the blocker preventing:**
- ✅ Preference checking
- ✅ Proper navigation
- ✅ Google Sign-In flow completion

---

**Last Updated:** December 27, 2025  
**Priority:** CRITICAL  
**Status:** READY TO TEST

