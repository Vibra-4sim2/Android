# 🔍 Avatar Mismatch - Debug Guide

## 🎯 Problem

**You're seeing DIFFERENT avatars** in these two places:
1. **Sortie Card** (Home/Explore) - Shows one picture
2. **User Profile** - Shows a different picture

**They should be the SAME** since both come from the database.

---

## 🔧 What I Added - Debug Logging

I've added **debug logging** to help us identify where the mismatch is happening.

### Logs Added:

#### 1. HomeExploreScreen (Sortie Cards)
```kotlin
Log.d("HomeExplore", "Sortie: ${sortie.titre}")
Log.d("HomeExplore", "Creator ID: ${sortie.createurId.id}")
Log.d("HomeExplore", "Creator Name: ${sortie.createurId.firstName} ${sortie.createurId.lastName}")
Log.d("HomeExplore", "Creator Avatar: ${sortie.createurId.avatar}")
```

#### 2. UserProfileScreen
```kotlin
Log.d("UserProfile", "User Name: $userName")
Log.d("UserProfile", "Avatar URL: $avatarUrl")
```

---

## 📊 How to Debug

### Step 1: Build and Run
1. Build the app
2. Open **Logcat** in Android Studio
3. Filter by tag: `HomeExplore` or `UserProfile`

### Step 2: Test the Flow
1. Go to **Home/Explore** screen
2. Look at a sortie card
3. **Note the avatar shown** in the card
4. **Check Logcat** - Look for:
   ```
   HomeExplore: Creator Avatar: [URL_HERE]
   ```
5. **Click on the avatar** to go to user's profile
6. **Note the avatar shown** in the profile
7. **Check Logcat** - Look for:
   ```
   UserProfile: Avatar URL: [URL_HERE]
   ```

### Step 3: Compare the URLs
- Are the two avatar URLs **THE SAME**?
  - **YES** → Problem is in the UI rendering (cache issue?)
  - **NO** → Problem is in the backend/API

---

## 🔍 Possible Causes

### Cause 1: Different API Responses
**Symptom:** Logcat shows different URLs

**Problem:** Backend returns different avatar values:
- `/sorties` API returns one avatar
- `/users/:id` API returns a different avatar

**Solution:** Backend needs to ensure both APIs return the same avatar field

---

### Cause 2: Coil Image Caching
**Symptom:** Logcat shows SAME URL, but different images display

**Problem:** Coil is caching old images

**Solution:** Clear app data or add cache-busting parameter

---

### Cause 3: Avatar Field Empty in Sortie
**Symptom:** Sortie card shows default image, profile shows real avatar

**Problem:** When backend populates `createurId` in sortie, it doesn't include avatar

**Solution:** Backend needs to populate avatar in `createurId` when returning sorties

---

## 📋 What to Check in Logcat

After clicking from sortie card → user profile, you should see:

```
D/HomeExplore: ═══════════════════════════════════
D/HomeExplore: Sortie: Morning Ride
D/HomeExplore: Creator ID: 507f1f77bcf86cd799439011
D/HomeExplore: Creator Name: John Doe
D/HomeExplore: Creator Avatar: https://example.com/avatars/john123.jpg
D/HomeExplore: ═══════════════════════════════════

[User clicks avatar]

D/UserProfile: ═══════════════════════════════════
D/UserProfile: User Name: John Doe
D/UserProfile: Avatar URL: https://example.com/avatars/john123.jpg
D/UserProfile: ═══════════════════════════════════
```

**The avatar URLs should MATCH!**

---

## ✅ Expected Behavior

Both places should show:
- **Same URL** in logs
- **Same picture** on screen

If they don't match, the problem is in the **backend API**.

---

## 🔧 Temporary Workaround (If Backend Can't Fix)

If the backend can't be fixed immediately, we can force refresh the avatar in UserProfileScreen by disabling Coil cache:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(avatarUrl)
        .diskCachePolicy(CachePolicy.DISABLED)  // Disable cache
        .memoryCachePolicy(CachePolicy.DISABLED)  // Disable cache
        .build(),
    ...
)
```

But this is NOT recommended - better to fix the backend.

---

## 📝 Next Steps

1. **Build and run the app**
2. **Open Logcat** (View → Tool Windows → Logcat)
3. **Filter** by tag: `HomeExplore` or `UserProfile`
4. **Test** the flow: Home/Explore → Click avatar → User Profile
5. **Copy the logs** showing both avatar URLs
6. **Share the logs** so we can identify the exact issue

---

## 🎯 Questions to Answer

Based on the logs:

1. **What is the avatar URL in the sortie card?**
2. **What is the avatar URL in the user profile?**
3. **Are they the same or different?**
4. **Is one of them null or empty?**

Once we have these answers, we can fix the exact problem!

---

**Debug logging added:** December 29, 2025  
**Status:** Ready to test  
**Next:** Run app and check Logcat

