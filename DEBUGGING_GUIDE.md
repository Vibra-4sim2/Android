# 🔍 DEBUGGING GUIDE - SortieDetailScreen Issues

## Current Status

Based on your logcat, I found that:
1. **Token IS working** for notifications (you can see Bearer token in the notification API calls)
2. **Token NOT found** by SortieDetailScreen (`token available: false`)

This means the token exists but isn't being retrieved correctly by SortieDetailScreen.

## What I Just Fixed

### 1. Enhanced Token Retrieval ✅
Added comprehensive token checking that looks in **3 different locations**:
- `auth_prefs` with key `access_token`
- `cycle_app_prefs` with key `auth_token`  
- `UserPreferences.getToken()` (which also checks cycle_app_prefs)

### 2. Added Extensive Logging ✅
Now when you open SortieDetailScreen, you'll see:
- All SharedPreferences keys
- Token values from each location
- Which token source is being used
- Final token that will be used

### 3. Enhanced Back Button ✅
Added detailed logging and fallback navigation:
- Shows navController backstack info
- Shows current destination
- Shows popBackStack result
- Falls back to home if pop fails

## Next Steps - TEST THIS NOW

### Step 1: Rebuild and Run
1. Build the app with the new changes
2. Login if not already logged in
3. Navigate to a sortie detail screen

### Step 2: Check the Logcat
Look for this output:
```
========== TOKEN DEBUG ==========
auth_prefs keys: [...]
auth_prefs.access_token: ...
cycle_app_prefs keys: [...]
cycle_app_prefs.auth_token: ...
UserPreferences.getToken(): ...
─────────────────────────────────
FINAL token: ...
FINAL userId: ...
Token available: true/false
==================================
```

### Step 3: Click Join Button
After clicking "Rejoindre l'aventure", check logcat for:
```
Join button clicked, token available: true/false
```

### Step 4: Click Back Button
After clicking the back arrow, check logcat for:
```
========== BACK BUTTON CLICKED ==========
NavController backstack count: ...
Current destination: ...
PopBackStack result: true/false
=========================================
```

## What to Report Back

Please send me the **COMPLETE logcat output** from when you:
1. Open the SortieDetailScreen (you'll see TOKEN DEBUG)
2. Click the join button
3. Click the back button

This will tell me exactly:
- Where the token actually is
- Why it's not being found
- Why navigation might be failing

## Expected Outcomes

### If Token is Found:
```
✅ Using token from cycle_app_prefs
FINAL token: eyJhbGciOiJIUzI1NiIsInR5cCI6...
Token available: true
```

Then join button should work!

### If Token is NOT Found:
```
❌ NO TOKEN FOUND IN ANY LOCATION!
FINAL token: ❌ EMPTY
Token available: false
```

This means the token truly isn't saved anywhere. Solution: Logout and login again.

### For Back Button:
```
PopBackStack result: true
```
Should navigate back successfully.

If result is `false`, it will automatically navigate to home instead.

## Quick Fix if Token Missing

If the logcat shows NO token in any location:

**Do this:**
1. Go to Settings → Logout
2. **Close the app completely**
3. **Reopen the app**
4. Login again
5. The token should now be saved to all locations

This is because I updated `UserPreferences.saveToken()` to save to BOTH:
- `cycle_app_prefs.auth_token`
- `auth_prefs.access_token`

## Files Changed

1. **SortieDetailScreen.kt**
   - Lines ~69-120: Comprehensive token retrieval from 3 sources
   - Lines ~227-244: Enhanced back button with logging and fallback
   - Lines ~246-257: Enhanced join button validation

2. **UserPreferences.kt** (from earlier fix)
   - Updated `saveToken()` to save to both locations
   - Updated `saveUserId()` to save to both locations

## Test and Report

**Please test now and send me the logcat output!** 

I need to see the "TOKEN DEBUG" section to understand where the token actually is (or isn't).

---

**Ready for testing!** 🚀

