# 🎨 Avatar Flow - Visual Guide

## 📊 Avatar Display Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     User Profile Data                            │
│                 (from database/backend)                          │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         │ avatar field
                         ▼
            ┌────────────────────────┐
            │  Is avatar null/empty? │
            └────────┬───────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
        ▼ YES                     ▼ NO
┌──────────────┐         ┌──────────────────┐
│ Show Fallback│         │  Try Load Avatar │
└──────┬───────┘         └────────┬─────────┘
       │                          │
       │                  ┌───────┴────────┐
       │                  │                │
       │             ✅ Success      ❌ Error
       │                  │                │
       │                  ▼                │
       │          ┌──────────────┐        │
       │          │ Show Avatar  │        │
       │          └──────────────┘        │
       │                                  │
       └──────────────┬───────────────────┘
                      │
                      ▼
        ┌─────────────────────────┐
        │   Select Fallback Type  │
        └────────┬────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
    ▼ UserAvatar              ▼ UserAvatarWithInitials
┌──────────────┐         ┌──────────────────┐
│Show Default  │         │  Has firstName   │
│Image         │         │  & lastName?     │
│(homme.jpeg)  │         └────────┬─────────┘
└──────────────┘                  │
                         ┌────────┴────────┐
                         │                 │
                         ▼ YES             ▼ NO
                  ┌──────────────┐  ┌──────────────┐
                  │Show Initials │  │Show Default  │
                  │  (e.g., JD)  │  │Image         │
                  └──────────────┘  └──────────────┘
```

---

## 🗺️ Screen-by-Screen Avatar Mapping

### 1. Profile Screen (Logged-in User)

```
┌─────────────────────────────────────┐
│         Profile Screen              │
├─────────────────────────────────────┤
│                                     │
│        ┌─────────────┐              │
│        │   Avatar    │ ← UserAvatar │
│        │   (80dp)    │              │
│        └─────────────┘              │
│                                     │
│        Camera Icon → Upload         │
│                                     │
│        User Name                    │
│        User Email                   │
│                                     │
│    [Edit Profile Button]            │
│                                     │
└─────────────────────────────────────┘

Avatar Source: currentUser.avatar
Fallback: R.drawable.homme
```

---

### 2. User Profile Screen (Other Users)

```
┌─────────────────────────────────────┐
│      User Profile Screen            │
├─────────────────────────────────────┤
│                                     │
│       ⭐⭐⭐⭐⭐ (4.8)               │
│                                     │
│        ┌─────────────┐              │
│        │   Avatar    │ ← UserAvatar │
│        │  (110dp)    │              │
│        └─────────────┘              │
│                                     │
│        User Name                    │
│        user@email.com               │
│        📍 Location                  │
│                                     │
│    [Follow] [Message]               │
│                                     │
│    Adventures | Participations      │
│                                     │
└─────────────────────────────────────┘

Avatar Source: user.avatar
Fallback: R.drawable.homme
```

---

### 3. Home/Explore Screen (Sortie Cards)

```
┌─────────────────────────────────────┐
│      Sortie Card                    │
├─────────────────────────────────────┤
│ ┌─────────────────────────────┐    │
│ │   [Sortie Photo]            │    │
│ │                             │    │
│ └─────────────────────────────┘    │
│                                     │
│  ┌───┐  Morning Ride                │
│  │ A │ ← UserAvatar (44dp)          │
│  └───┘  Creator Name                │
│         📍 Location                 │
│                                     │
│  🚴 Cycling  ⏰ 8:30 AM             │
│  👥 12/20    📏 25 km               │
│                                     │
└─────────────────────────────────────┘

Avatar Source: sortie.createurId.avatar
Fallback Option 1: R.drawable.homme
Fallback Option 2: First letter of email
```

---

### 4. Feed Screen (Publication Cards)

```
┌─────────────────────────────────────┐
│      Publication Card               │
├─────────────────────────────────────┤
│                                     │
│  ┌───┐  John Doe                    │
│  │JD │ ← UserAvatarWithInitials     │
│  └───┘  2 hours ago                 │
│                                     │
│  Just finished an amazing ride! 🚴  │
│                                     │
│  ┌─────────────────────────────┐   │
│  │   [Post Image]              │   │
│  │                             │   │
│  └─────────────────────────────┘   │
│                                     │
│  #cycling #adventure #nature        │
│  📍 La Marsa                        │
│                                     │
│  ❤️ 24  💬 5  ↗️ 2                 │
│                                     │
│  [Like] [Comment] [Share]           │
│                                     │
└─────────────────────────────────────┘

Avatar Source: publication.author.avatar
Fallback: Initials from firstName + lastName
Background: Dark gray (#374151)
Text Color: Green (#4ADE80)
```

---

## 🎯 Avatar Size Guidelines

| Location | Size | Shape | Border |
|----------|------|-------|--------|
| **Profile Screen** (own) | 80dp - 100dp | Circle | 3dp Green |
| **User Profile** (others) | 110dp | Circle | 3dp Green |
| **Sortie Card** | 44dp | Circle | 2dp Green |
| **Publication Card** | 50dp | Circle | 2dp Green |
| **Comments** (future) | 32dp | Circle | 1dp |
| **Small Badges** | 24dp | Circle | None |

---

## 🎨 Color Scheme for Fallbacks

### Default Colors (Feed Screen)
```kotlin
backgroundColor = Color(0xFF374151)  // Dark Gray
textColor = Color(0xFF4ADE80)        // Green Accent
```

### Visual:
```
┌─────────┐
│         │
│   JD    │  ← Green text (#4ADE80)
│         │
└─────────┘
    ↑
Dark gray background (#374151)
```

---

## 📦 Data Models Reference

### UserProfileResponse
```kotlin
data class UserProfileResponse(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatar: String? = null,  // ← Can be null!
    // ...other fields
)
```

### CreateurInfo (Sortie Creator)
```kotlin
data class CreateurInfo(
    val _id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val avatar: String? = null  // ← Can be null!
)
```

### AuthorData (Publication Author)
```kotlin
data class AuthorData(
    val _id: String,
    val firstName: String,
    val lastName: String,
    val avatar: String? = null  // ← Can be null!
)
```

---

## 🔄 Upload Flow (Unchanged)

```
┌─────────────────────┐
│  User clicks        │
│  camera icon        │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Image Picker       │
│  Opens              │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  User selects       │
│  image              │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  uploadAvatar()     │
│  is called          │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Image uploaded     │
│  to backend         │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Profile reloaded   │
│  New avatar shown   │
└─────────────────────┘

✅ This flow is PRESERVED - no changes!
```

---

## 🛡️ Error Handling Flow

```
Avatar Load Attempt
        │
        ├─ Network Error → Show fallback
        ├─ 404 Not Found → Show fallback
        ├─ Invalid URL → Show fallback
        ├─ Timeout → Show fallback
        ├─ CORS Error → Show fallback
        └─ Success → Show avatar ✅
```

---

## 📱 Real-World Examples

### Example 1: New User (No Avatar)

**Data:**
```json
{
  "_id": "123",
  "firstName": "Alice",
  "lastName": "Johnson",
  "email": "alice@example.com",
  "avatar": null
}
```

**Result:**
- Profile Screen: Shows `R.drawable.homme`
- Feed Screen: Shows "AJ" in colored circle

---

### Example 2: User with Avatar

**Data:**
```json
{
  "_id": "456",
  "firstName": "Bob",
  "lastName": "Smith",
  "email": "bob@example.com",
  "avatar": "https://cdn.example.com/avatars/bob.jpg"
}
```

**Result:**
- All screens: Shows avatar from URL
- If load fails: Shows `R.drawable.homme`

---

### Example 3: Empty Avatar String

**Data:**
```json
{
  "_id": "789",
  "firstName": "Carol",
  "lastName": "White",
  "email": "carol@example.com",
  "avatar": ""
}
```

**Result:**
- Treated as null
- Shows fallback (homme.jpeg or initials)

---

## 🎓 Summary

**Key Takeaways:**
1. All avatar scenarios are handled safely
2. Fallback is always shown (never blank)
3. Upload feature is preserved
4. Consistent UX across all screens
5. No crashes on missing data

**Avatar Priority:**
1. Try loading from URL
2. On failure → Show fallback
3. Fallback = Default image OR initials

---

**Visual Guide Version:** 1.0  
**Last Updated:** December 29, 2025

