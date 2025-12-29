# 🎯 Quick Reference - Avatar Utilities

## How to Use the New Avatar Components

### Option 1: Simple Avatar with Default Fallback

Use `UserAvatar()` when you just want to show an avatar with a default image fallback.

```kotlin
import com.example.dam.utils.UserAvatar

UserAvatar(
    avatarUrl = user.avatar,  // Can be null, empty, or URL
    modifier = Modifier.size(80.dp)
)
```

**Behavior:**
- ✅ If `avatarUrl` is valid → Shows the avatar
- ✅ If `avatarUrl` is null/empty → Shows `R.drawable.homme`
- ✅ If load fails → Shows `R.drawable.homme`

---

### Option 2: Avatar with Initials Fallback

Use `UserAvatarWithInitials()` when you want to show user initials if no avatar exists.

```kotlin
import com.example.dam.utils.UserAvatarWithInitials

UserAvatarWithInitials(
    avatarUrl = user.avatar,
    firstName = user.firstName,
    lastName = user.lastName,
    modifier = Modifier.size(80.dp),
    backgroundColor = Color(0xFF374151),  // Dark gray
    textColor = Color(0xFF4ADE80)         // Green
)
```

**Behavior:**
- ✅ If `avatarUrl` is valid → Shows the avatar
- ✅ If `avatarUrl` is null/empty → Shows initials (e.g., "JD")
- ✅ If load fails → Shows `R.drawable.homme`

---

## 📍 Where Each Is Used

### `UserAvatar` - Simple Default Image Fallback

| Screen | Location | Used For |
|--------|----------|----------|
| **profileScreen.kt** | Profile header | Logged-in user's avatar |
| **UserProfileScreen.kt** | Profile header | Other users' avatars |
| **HomeExploreScreen.kt** | Sortie cards | Sortie creator avatars |

### `UserAvatarWithInitials` - Initials Fallback

| Screen | Location | Used For |
|--------|----------|----------|
| **FeedScreen.kt** | Publication cards | Publication author avatars |

---

## 🔧 Parameters Explained

### `UserAvatar`

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `avatarUrl` | `String?` | - | **Required.** URL of the avatar (can be null) |
| `modifier` | `Modifier` | `Modifier` | Size, shape, padding, etc. |
| `contentDescription` | `String` | "User Avatar" | Accessibility description |
| `contentScale` | `ContentScale` | `ContentScale.Crop` | How to scale the image |
| `fallbackDrawable` | `Int` | `R.drawable.homme` | Default image resource ID |

### `UserAvatarWithInitials`

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `avatarUrl` | `String?` | - | **Required.** URL of the avatar (can be null) |
| `firstName` | `String?` | - | **Required.** User's first name for initials |
| `lastName` | `String?` | - | **Required.** User's last name for initials |
| `modifier` | `Modifier` | `Modifier` | Size, shape, padding, etc. |
| `contentDescription` | `String` | "User Avatar" | Accessibility description |
| `backgroundColor` | `Color` | `Color(0xFF374151)` | Background color for initials circle |
| `textColor` | `Color` | `Color(0xFF4ADE80)` | Text color for initials |

---

## 💡 Examples

### Example 1: Profile Avatar (80dp, circular)

```kotlin
Box(
    modifier = Modifier
        .size(80.dp)
        .clip(CircleShape)
        .border(2.dp, Color.Green, CircleShape)
) {
    UserAvatar(
        avatarUrl = currentUser?.avatar,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Example 2: Small Avatar (40dp, with initials)

```kotlin
Box(
    modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
) {
    UserAvatarWithInitials(
        avatarUrl = author.avatar,
        firstName = author.firstName,
        lastName = author.lastName,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Example 3: Custom Colors for Initials

```kotlin
UserAvatarWithInitials(
    avatarUrl = user.avatar,
    firstName = user.firstName,
    lastName = user.lastName,
    modifier = Modifier.size(60.dp),
    backgroundColor = Color(0xFF1A1A1A),  // Dark background
    textColor = Color(0xFFFFD700)         // Gold text
)
```

---

## ⚠️ Common Mistakes to Avoid

### ❌ DON'T manually check if avatar is null
```kotlin
// BAD - Don't do this
if (user.avatar != null) {
    AsyncImage(model = user.avatar, ...)
} else {
    Image(painter = painterResource(R.drawable.homme), ...)
}
```

### ✅ DO use the utility
```kotlin
// GOOD - Use the utility
UserAvatar(
    avatarUrl = user.avatar,
    modifier = Modifier.fillMaxSize()
)
```

---

### ❌ DON'T forget to apply modifier

```kotlin
// BAD - Avatar won't have correct size
UserAvatar(avatarUrl = user.avatar)
```

### ✅ DO provide modifier for size

```kotlin
// GOOD - Avatar has correct size
UserAvatar(
    avatarUrl = user.avatar,
    modifier = Modifier.size(80.dp)
)
```

---

## 🎨 Styling Tips

### Circular Avatar with Border

```kotlin
Box(
    modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .border(3.dp, GreenAccent, CircleShape)
        .background(CardDark)
) {
    UserAvatar(
        avatarUrl = user.avatar,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Square Avatar with Rounded Corners

```kotlin
Box(
    modifier = Modifier
        .size(80.dp)
        .clip(RoundedCornerShape(12.dp))
) {
    UserAvatar(
        avatarUrl = user.avatar,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Avatar with Badge/Indicator

```kotlin
Box(modifier = Modifier.size(80.dp)) {
    UserAvatar(
        avatarUrl = user.avatar,
        modifier = Modifier.fillMaxSize()
    )
    
    // Online indicator
    Box(
        modifier = Modifier
            .size(16.dp)
            .align(Alignment.BottomEnd)
            .background(Color.Green, CircleShape)
            .border(2.dp, Color.White, CircleShape)
    )
}
```

---

## 🧪 Testing Scenarios

### Scenario 1: User with Avatar
```kotlin
val user = UserProfileResponse(
    avatar = "https://example.com/avatar.jpg",
    firstName = "John",
    lastName = "Doe"
)

UserAvatar(avatarUrl = user.avatar, modifier = Modifier.size(80.dp))
// ✅ Shows the avatar from URL
```

### Scenario 2: User without Avatar
```kotlin
val user = UserProfileResponse(
    avatar = null,
    firstName = "Jane",
    lastName = "Smith"
)

UserAvatarWithInitials(
    avatarUrl = user.avatar,
    firstName = user.firstName,
    lastName = user.lastName,
    modifier = Modifier.size(80.dp)
)
// ✅ Shows "JS" in colored circle
```

### Scenario 3: Empty Avatar String
```kotlin
val user = UserProfileResponse(
    avatar = "",  // Empty string
    firstName = "Bob",
    lastName = "Wilson"
)

UserAvatar(avatarUrl = user.avatar, modifier = Modifier.size(80.dp))
// ✅ Shows R.drawable.homme (default image)
```

---

## 📚 Additional Resources

- **File Location:** `app/src/main/java/com/example/dam/utils/ImageUtils.kt`
- **Coil Documentation:** https://coil-kt.github.io/coil/compose/
- **Implementation Guide:** See `AVATAR_FIX_COMPLETE.md`

---

## 🆘 Troubleshooting

### Avatar not showing?

1. Check if `avatarUrl` is valid:
   ```kotlin
   Log.d("Avatar", "URL: ${user.avatar}")
   ```

2. Check internet permission in `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```

3. Check if default image exists:
   - Location: `app/src/main/res/drawable/homme.jpeg`

### Initials not showing correctly?

1. Verify firstName and lastName are not null:
   ```kotlin
   Log.d("Avatar", "Name: ${user.firstName} ${user.lastName}")
   ```

2. Check if background color has sufficient contrast

### Build error "Cannot find symbol: UserAvatar"?

1. Add import:
   ```kotlin
   import com.example.dam.utils.UserAvatar
   ```

2. Sync project with Gradle files

---

**Questions? Check `AVATAR_FIX_COMPLETE.md` for full documentation.**

