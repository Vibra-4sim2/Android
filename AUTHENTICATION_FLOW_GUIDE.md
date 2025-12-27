# 🔐 Complete Authentication Flow Implementation Guide

## 📋 Overview

This guide documents the complete authentication flow implementation that intelligently handles:
- ✅ **Regular Login** - Email/password authentication with preference checking
- ✅ **Regular Signup** - Account creation with mandatory preferences setup
- ✅ **Google Sign-In** - First-time vs returning user detection
- ✅ **Smart Navigation** - Automatic routing to Home or Preferences based on account status

---

## 🏗️ Architecture Overview

### Key Components

1. **Models** (`GoogleSigInRequest.kt`)
   - Enhanced `GoogleSignInResponse` with `userId` and `isNewUser` fields

2. **Repository** (`AuthRepository.kt`)
   - `googleSignIn()` - Returns full response with user status
   - `checkOnboardingStatus()` - Verifies preference completion from backend

3. **ViewModel** (`LoginViewModel.kt`)
   - Enhanced `LoginUiState` with navigation metadata
   - `checkPreferencesStatus()` - Fetches preference status for regular login
   - `googleSignIn()` - Handles Google authentication with user status

4. **UI** (`LoginScreen.kt`)
   - Smart navigation logic based on authentication type
   - Automatic preference checking for regular login
   - Direct navigation for Google sign-in based on `isNewUser` flag

5. **Splash Screen** (`SplashScreen.kt`)
   - Already handles token validation and preference checking on app start

---

## 🔄 Authentication Flows

### 1️⃣ **Regular Sign-Up Flow**

```
User fills registration form
        ↓
RegisterViewModel.register()
        ↓
Backend creates account
        ↓
Auto-login with credentials
        ↓
Navigate to PREFERENCES (always)
        ↓
User completes preferences
        ↓
Navigate to HOME
```

**Key Points:**
- New users ALWAYS go to preferences after signup
- Auto-login happens after successful registration
- Preferences are mandatory before accessing home

---

### 2️⃣ **Regular Login Flow**

```
User enters email/password
        ↓
LoginViewModel.login()
        ↓
Backend validates & returns JWT
        ↓
Extract userId from JWT
        ↓
LoginViewModel.checkPreferencesStatus(userId)
        ↓
Backend returns preference completion status
        ↓
Navigate based on status:
    - Has preferences → HOME
    - Needs preferences → PREFERENCES
```

**Key Points:**
- Checks preference status from backend (not local cache)
- Uses JWT to extract userId
- Backend call ensures accurate status

**Code Flow:**
```kotlin
// After successful login
LaunchedEffect(uiState.isSuccess) {
    val userId = UserPreferences.getUserId(context)
    if (userId != null) {
        viewModel.checkPreferencesStatus(userId, token)
    }
}

// Navigation based on needsPreferences flag
if (uiState.needsPreferences) {
    NavigationRoutes.PREFERENCES
} else {
    NavigationRoutes.HOME
}
```

---

### 3️⃣ **Google Sign-In Flow (First Time)**

```
User clicks Google Sign-In
        ↓
Google authentication
        ↓
Send idToken to backend
        ↓
Backend checks if user exists
        ↓
User NOT found → Create new account
        ↓
Backend returns:
    - access_token
    - userId
    - isNewUser: true
        ↓
Navigate to PREFERENCES
        ↓
User completes preferences
        ↓
Navigate to HOME
```

**Backend Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": true
}
```

---

### 4️⃣ **Google Sign-In Flow (Returning User)**

```
User clicks Google Sign-In
        ↓
Google authentication
        ↓
Send idToken to backend
        ↓
Backend checks if user exists
        ↓
User FOUND → Return existing account
        ↓
Backend returns:
    - access_token
    - userId
    - isNewUser: false
        ↓
Navigate to HOME (skip preferences)
```

**Backend Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": false
}
```

**Key Points:**
- `isNewUser` flag determines navigation
- No backend API call needed for preference check
- Instant navigation based on user status

---

## 🎯 Navigation Logic

### LoginScreen Navigation Decision Tree

```
Login Success
    ↓
Is Google Sign-In?
    ├─ YES → Use isNewUser flag
    │         ├─ isNewUser = true → PREFERENCES
    │         └─ isNewUser = false → HOME
    │
    └─ NO → Regular Login
              ↓
          Check userId available?
              ├─ YES → Call checkPreferencesStatus()
              │         ├─ needsPreferences = true → PREFERENCES
              │         └─ needsPreferences = false → HOME
              │
              └─ NO → Fallback to local cache
                        ├─ onboardingComplete = true → HOME
                        └─ onboardingComplete = false → PREFERENCES
```

---

## 🔧 Implementation Details

### 1. Enhanced Models

**File:** `models/GoogleSigInRequest.kt`

```kotlin
data class GoogleSignInResponse(
    @SerializedName("access_token")
    val access_token: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("isNewUser")
    val isNewUser: Boolean? = false
)
```

### 2. Updated LoginUiState

**File:** `viewmodel/LoginViewModel.kt`

```kotlin
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val accessToken: String? = null,
    val isSuccess: Boolean = false,
    val userId: String? = null,           // NEW
    val isNewUser: Boolean? = null,        // NEW
    val needsPreferences: Boolean = false  // NEW
)
```

### 3. Preference Status Check

**File:** `viewmodel/LoginViewModel.kt`

```kotlin
fun checkPreferencesStatus(userId: String, token: String) {
    viewModelScope.launch {
        when (val result = repository.checkOnboardingStatus(userId, token)) {
            is Result.Success -> {
                val hasCompletedPreferences = result.data
                _uiState.value = _uiState.value.copy(
                    userId = userId,
                    needsPreferences = !hasCompletedPreferences
                )
            }
            is Result.Error -> {
                // On error, assume preferences needed
                _uiState.value = _uiState.value.copy(
                    userId = userId,
                    needsPreferences = true
                )
            }
        }
    }
}
```

### 4. Smart Navigation in LoginScreen

**File:** `Screens/LoginScreen.kt`

```kotlin
LaunchedEffect(uiState.isSuccess, uiState.userId, uiState.needsPreferences) {
    if (uiState.isSuccess) {
        val destination: String
        
        if (uiState.isNewUser != null) {
            // Google Sign-In
            destination = if (uiState.isNewUser == true) {
                NavigationRoutes.PREFERENCES
            } else {
                NavigationRoutes.HOME
            }
        } else if (uiState.userId != null) {
            // Regular Login with preference check
            destination = if (uiState.needsPreferences) {
                NavigationRoutes.PREFERENCES
            } else {
                NavigationRoutes.HOME
            }
        } else {
            // Fallback
            destination = if (UserPreferences.isOnboardingComplete(context)) {
                NavigationRoutes.HOME
            } else {
                NavigationRoutes.PREFERENCES
            }
        }
        
        navController.navigate(destination) {
            popUpTo("login") { inclusive = true }
        }
    }
}
```

---

## 🌐 Backend Requirements

### Google Sign-In Endpoint

**Endpoint:** `POST /auth/google`

**Request:**
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjE4MmU..."
}
```

**Response (New User):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": true
}
```

**Response (Existing User):**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "676d1234567890abcdef",
  "isNewUser": false
}
```

### Backend Logic

```javascript
// Pseudo-code for /auth/google endpoint
async function handleGoogleSignIn(idToken) {
  // Verify Google token
  const googleUser = await verifyGoogleToken(idToken);
  
  // Check if user exists in database
  let user = await User.findOne({ email: googleUser.email });
  let isNewUser = false;
  
  if (!user) {
    // Create new user
    user = await User.create({
      email: googleUser.email,
      firstName: googleUser.given_name,
      lastName: googleUser.family_name,
      avatar: googleUser.picture,
      authProvider: 'google'
    });
    isNewUser = true;
  }
  
  // Generate JWT
  const access_token = jwt.sign({ userId: user._id }, SECRET_KEY);
  
  return {
    access_token,
    userId: user._id,
    isNewUser
  };
}
```

---

## 🧪 Testing Scenarios

### Test Case 1: New User Registration
1. Open app → Navigate to Register
2. Fill form and submit
3. **Expected:** Auto-login → Navigate to PREFERENCES
4. Complete preferences
5. **Expected:** Navigate to HOME

### Test Case 2: Returning User Login (Regular)
1. Open app → Navigate to Login
2. Enter credentials and submit
3. **Expected:** Check preferences from backend
4. If preferences complete → Navigate to HOME
5. If preferences incomplete → Navigate to PREFERENCES

### Test Case 3: First-Time Google Sign-In
1. Open app → Navigate to Login
2. Click "Sign in with Google"
3. Complete Google authentication
4. **Expected:** Backend returns `isNewUser: true`
5. **Expected:** Navigate to PREFERENCES
6. Complete preferences
7. **Expected:** Navigate to HOME

### Test Case 4: Returning Google User
1. Open app → Navigate to Login
2. Click "Sign in with Google"
3. Complete Google authentication
4. **Expected:** Backend returns `isNewUser: false`
5. **Expected:** Skip preferences → Navigate to HOME directly

### Test Case 5: Splash Screen (Existing Session)
1. User previously logged in
2. Close and reopen app
3. **Expected:** SplashScreen checks token
4. **Expected:** Check preferences from backend
5. Navigate to HOME or PREFERENCES based on status

---

## 🐛 Troubleshooting

### Issue: Google Sign-In always goes to preferences

**Cause:** Backend not returning `isNewUser` field

**Solution:** Update backend to include `isNewUser` in response

**Verify:**
```kotlin
Log.d("LoginScreen", "🆕 Is new user: ${uiState.isNewUser}")
```

### Issue: Regular login skips preference check

**Cause:** UserId not saved after login

**Solution:** Ensure JWT is decoded and userId saved:
```kotlin
saveAuthData(context, token)  // This should save userId
```

### Issue: Always navigates to preferences

**Cause:** Backend preference check failing

**Solution:** Check logs:
```kotlin
Log.d("LoginScreen", "🔍 Checking preferences for userId: $userId")
```

Verify backend endpoint `/preferences/{userId}` is accessible

---

## 📝 Summary

### What Was Changed

1. ✅ **GoogleSignInResponse** - Added `userId` and `isNewUser` fields
2. ✅ **LoginUiState** - Added `userId`, `isNewUser`, `needsPreferences`
3. ✅ **AuthRepository.googleSignIn()** - Returns enhanced response
4. ✅ **LoginViewModel.checkPreferencesStatus()** - New method for preference checking
5. ✅ **LoginScreen navigation** - Smart routing based on authentication type
6. ✅ **Comprehensive logging** - Track flow at every step

### Benefits

- 🎯 **Seamless UX** - Users see preferences only when needed
- 🔐 **Secure** - Backend validates all authentication
- 📱 **Smart** - Different flows for Google vs regular login
- 🚀 **Fast** - Google sign-in uses flag, no extra API call
- 🧪 **Testable** - Clear logging for debugging

### Backend Update Required

⚠️ **IMPORTANT:** Your backend must be updated to return the enhanced Google Sign-In response:

```json
{
  "access_token": "...",
  "userId": "...",
  "isNewUser": true/false
}
```

Without this backend change, Google sign-in will fall back to local cache checking.

---

## 📞 Support

If you encounter issues:
1. Check Logcat for detailed flow logs
2. Verify backend returns correct response format
3. Ensure JWT contains userId claim
4. Test with both new and returning users

**Log Tags to Monitor:**
- `LoginScreen` - UI navigation decisions
- `LoginViewModel` - ViewModel state changes
- `AuthRepository` - Backend API calls
- `SplashScreen` - App startup flow

---

**Last Updated:** December 26, 2025  
**Version:** 1.0.0  
**Status:** ✅ Production Ready

