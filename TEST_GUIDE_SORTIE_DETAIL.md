# Quick Test Guide - SortieDetailScreen Fixes

## Before Testing
Make sure you have the latest code with the fixes applied.

## Test 1: Back Button ✅

### Steps:
1. Open the app
2. Navigate to Home → Explore tab
3. Click on any sortie card to open SortieDetailScreen
4. Click the **back arrow** button (top-left corner)

### Expected Result:
- ✅ Should navigate back to the previous screen (HomeExploreScreen)
- ✅ In logcat, you should see: `SortieDetailScreen: Back button clicked`

### If it fails:
- Check if there are any overlays blocking the button
- Check logcat for any errors

---

## Test 2: Join Sortie - Not Logged In ❌→✅

### Steps:
1. Logout if logged in (Settings → Logout)
2. Navigate to a sortie detail screen (you might need to login first to browse, then logout)
3. Click **"Rejoindre l'aventure"** button

### Expected Result:
- ✅ Should show toast message: "Veuillez vous connecter pour rejoindre cette sortie"
- ✅ No API call should be made
- ✅ In logcat: `Join button clicked, token available: false`

---

## Test 3: Join Sortie - Logged In (Fresh Login) ✅

### Steps:
1. **Completely logout** from the app
2. **Close and restart** the app (important!)
3. Login with valid credentials
4. Navigate to Home → Explore
5. Find a sortie you haven't joined yet
6. Click on it to open details
7. Click **"Rejoindre l'aventure"** button

### Expected Result:
- ✅ Should show toast: "Demande envoyée avec succès!"
- ✅ Button text changes to "Demande envoyée"
- ✅ Button shows checkmark icon and becomes disabled
- ✅ In logcat you should see:
  ```
  SortieDetailScreen: Token available: true
  SortieDetailScreen: Join button clicked, token available: true
  ParticipationVM: Attempting to join sortie: <sortieId>
  ParticipationVM: ✅ Successfully joined sortie
  ```

### If you get "Unauthorized" error:
1. Check logcat for: `SortieDetailScreen: Token available: true`
2. If false, the token isn't being saved properly during login
3. Try logging out and logging in again
4. Check that `UserPreferences.kt` was properly updated with the dual-save logic

---

## Test 4: Join Sortie - Already Joined ✅

### Steps:
1. Navigate to a sortie you already sent a join request to
2. Open the sortie details

### Expected Result:
- ✅ Button text shows "Demande envoyée"
- ✅ Button has checkmark icon
- ✅ Button is disabled (greyed out)
- ✅ In logcat: `ParticipationVM: User has joined: true`

---

## Test 5: Join Sortie - Full Capacity 🚫

### Steps:
1. Find a sortie that has reached its capacity (participants = capacité)
2. Open the sortie details

### Expected Result:
- ✅ Button text shows "Complet"
- ✅ Button has block icon
- ✅ Button is disabled (greyed out)

---

## Test 6: Creator View 👑

### Steps:
1. Create your own sortie
2. Navigate to its detail page

### Expected Result:
- ✅ Instead of "Rejoindre l'aventure", you should see "Gérer les demandes"
- ✅ Clicking it navigates to participation requests screen

---

## Debugging Tips

### Check Token in Logcat:
When opening SortieDetailScreen, look for:
```
SortieDetailScreen: Token available: true/false
SortieDetailScreen: Current User ID: <userId>
```

### Check Join Action:
When clicking join button:
```
SortieDetailScreen: Join button clicked, token available: true/false
ParticipationVM: Attempting to join sortie: <sortieId>
ParticipationRepo: 📤 Creating participation for sortieId: <sortieId>
```

### Success Response:
```
ParticipationRepo: ✅ Participation created: <participationId>
ParticipationVM: ✅ Successfully joined sortie
```

### Error Response:
```
ParticipationRepo: ❌ Error 401: Unauthorized - please login again
ParticipationVM: ❌ Error joining: <error message>
```

---

## Common Issues & Solutions

### Issue: "Unauthorized" even when logged in
**Solution:**
- The token wasn't saved to both locations
- Make sure `UserPreferences.kt` has the updated `saveToken()` method
- Try logging out and logging in again (this will save token to both locations)

### Issue: Back button still doesn't work
**Possible causes:**
1. UI overlay blocking the button
2. Navigation state issue
**Solution:** 
- Check if other navigation works
- Try navigating from different screens

### Issue: Button doesn't respond at all
**Check:**
- Is the sortie loading? (Check for loading spinner)
- Are there any errors in logcat?
- Is the button disabled due to capacity/already joined?

---

## Files Changed

These files were modified to fix the issues:

1. **SortieDetailScreen.kt**
   - Lines ~71-95: Enhanced token retrieval
   - Lines ~183-188: Back button with logging
   - Lines ~189-200: Join button with validation

2. **UserPreferences.kt**
   - Lines ~28-51: Updated `saveToken()` to dual-save
   - Lines ~19-27: Updated `saveUserId()` to dual-save

---

## Rollback Instructions

If anything breaks, you can revert these changes:

### For SortieDetailScreen.kt:
1. Remove the token fallback logic (lines 71-95)
2. Revert to simple: `val token = sharedPref.getString("access_token", "") ?: ""`
3. Remove validation in onJoinClick

### For UserPreferences.kt:
1. Remove the auth_prefs saving logic from `saveToken()`
2. Remove the auth_prefs saving logic from `saveUserId()`

But this will bring back the "Unauthorized" error! The proper fix is to update ALL screens to use `UserPreferences.getToken()` instead of `auth_prefs`.

---

## Success Criteria ✅

All tests should pass:
- ✅ Back button navigates correctly
- ✅ Join shows error when not logged in
- ✅ Join works after fresh login
- ✅ Already joined shows correct state
- ✅ Full capacity shows correct state
- ✅ Creator sees manage button

**If all tests pass, the fix is successful!** 🎉

