# 🔴 Badge System Verification Guide

## ✅ Current Implementation Status

The badge system has been **fully implemented** with the immediate removal fix. Here's how it works:

### 📋 Badge Display Logic

The badge (red notification count) on each chat in the Messages List follows this simple rule:

```kotlin
val effectiveUnreadCount = if (isCurrentlyViewing) {
    0  // Hide badge when user is viewing the chat
} else {
    group.unreadCount  // Show backend's unread count
}
```

### 🔄 Complete User Flow

#### Scenario 1: User Opens Chat and Reads Messages

1. **User opens MessagesListScreen**
   - Each chat shows `unreadCount` from backend
   - Example: Chat A has badge showing "3"

2. **User taps on Chat A**
   - Navigate to `ChatConversationScreen`
   - `ChatStateManager.markChatAsOpened(sortieId)` is called
   - Chat A is added to `recentlyOpenedChats` set
   - Badge immediately becomes 0 (optimistic UI update)

3. **User reads messages in Chat A**
   - Messages are displayed
   - Backend receives "mark as read" events via WebSocket
   - Backend updates `unreadCount = 0` for Chat A

4. **User presses Back to return to MessagesListScreen**
   - `DisposableEffect.onDispose` is triggered
   - `ChatStateManager.clearOptimisticState(sortieId)` is called
   - Chat A is **immediately removed** from `recentlyOpenedChats`
   - Backend's `unreadCount` is now trusted

5. **MessagesListScreen refreshes**
   - `isCurrentlyViewing = false` (Chat A not in set)
   - `effectiveUnreadCount = backend.unreadCount = 0`
   - ✅ **Badge shows 0 or is hidden** (correct!)

#### Scenario 2: New Message Arrives

Continuing from above scenario...

6. **New message arrives in Chat A**
   - Backend receives new message
   - Backend updates `unreadCount = 1` for Chat A
   - WebSocket notifies app
   - `MessagesListScreen` refreshes

7. **Badge appears immediately**
   - `isCurrentlyViewing = false` (user not in chat)
   - `effectiveUnreadCount = backend.unreadCount = 1`
   - ✅ **Badge shows "1"** (correct!)

8. **User opens Chat A again**
   - `ChatStateManager.markChatAsOpened(sortieId)` called
   - Chat A added to `recentlyOpenedChats`
   - Badge immediately becomes 0
   - ✅ **Badge hides while viewing** (correct!)

### 🔧 Key Components

#### 1. ChatStateManager.kt

```kotlin
object ChatStateManager {
    private val _recentlyOpenedChats = MutableStateFlow<Set<String>>(emptySet())
    val recentlyOpenedChats: StateFlow<Set<String>> = _recentlyOpenedChats.asStateFlow()

    // Mark chat as currently being viewed
    fun markChatAsOpened(sortieId: String) {
        _recentlyOpenedChats.value = _recentlyOpenedChats.value + sortieId
    }

    // ✅ IMMEDIATE REMOVAL - No grace period!
    fun clearOptimisticState(sortieId: String, removeLastSeenCount: Boolean = false) {
        _recentlyOpenedChats.value = _recentlyOpenedChats.value - sortieId
        // Removed immediately, not after 3 seconds
    }
}
```

#### 2. ChatConversationScreen.kt

```kotlin
@Composable
fun ChatConversationScreen(...) {
    // When screen appears
    LaunchedEffect(sortieId) {
        ChatStateManager.markChatAsOpened(sortieId)
    }

    // When screen disappears
    DisposableEffect(Unit) {
        onDispose {
            ChatStateManager.clearOptimisticState(sortieId)
        }
    }
}
```

#### 3. MessagesListScreen.kt

```kotlin
@Composable
fun GroupChatItem(group: GroupChat) {
    val recentlyOpenedChats by ChatStateManager.recentlyOpenedChats.collectAsState()
    val isCurrentlyViewing = recentlyOpenedChats.contains(group.sortieId)

    val effectiveUnreadCount = if (isCurrentlyViewing) {
        0  // Hide badge when viewing
    } else {
        group.unreadCount  // Show backend count
    }

    // Display badge with effectiveUnreadCount
}
```

### ✅ Expected Behavior

| Situation | Badge State | Why |
|-----------|-------------|-----|
| Chat has 3 unread messages | Shows "3" | Backend's unreadCount = 3 |
| User opens that chat | Shows "0" (hidden) | isCurrentlyViewing = true |
| User reads all messages | Still "0" while viewing | isCurrentlyViewing = true |
| User goes back to list | Shows "0" or hidden | Backend unreadCount = 0 |
| New message arrives | Shows "1" | Backend unreadCount = 1 |
| User opens chat again | Shows "0" (hidden) | isCurrentlyViewing = true again |

### 🐛 Previous Bug (FIXED)

#### What was broken:

```kotlin
// ❌ OLD CODE
fun clearOptimisticState(sortieId: String) {
    GlobalScope.launch {
        delay(3000)  // Wait 3 seconds before removing
        _recentlyOpenedChats.value = _recentlyOpenedChats.value - sortieId
    }
}
```

**Problem**: When user left the chat, it stayed in `recentlyOpenedChats` for 3 more seconds. During this time:
- Badge showed 0 even though backend had unreadCount > 0
- New messages didn't show badges until 3 seconds passed

#### What was fixed:

```kotlin
// ✅ NEW CODE
fun clearOptimisticState(sortieId: String) {
    _recentlyOpenedChats.value = _recentlyOpenedChats.value - sortieId
    // Immediate removal, no delay!
}
```

**Result**: Badge immediately reflects backend's unreadCount when user leaves chat.

### 🧪 Testing Checklist

To verify the badge system is working:

- [ ] **Test 1: Badge disappears when opening chat**
  1. Find a chat with badge showing "3"
  2. Tap to open it
  3. ✅ Badge should become 0 immediately

- [ ] **Test 2: Badge stays gone after reading**
  1. Read all messages in chat
  2. Go back to messages list
  3. ✅ Badge should be 0 or hidden

- [ ] **Test 3: Badge reappears for new messages**
  1. Have someone send you a new message
  2. Check messages list
  3. ✅ Badge should show "1" immediately

- [ ] **Test 4: Badge hides again when reopening**
  1. Open the chat with new message
  2. ✅ Badge should become 0 immediately

- [ ] **Test 5: Multiple chats work independently**
  1. Open Chat A (badge disappears)
  2. Go back (badge stays 0)
  3. Open Chat B with unread messages
  4. ✅ Chat B badge disappears, Chat A badge unchanged

### 🔍 Debugging

If badges are not working correctly, check these logs:

```kotlin
// ChatStateManager logs
"✅ MARKING CHAT AS CURRENTLY VIEWING"
"🧹 REMOVING from currently viewing"

// MessagesListScreen logs
"🚫 Badge=0 (currently viewing)"
"📊 Badge={count} (from backend)"
```

### 📝 Summary

The badge system is **fully functional** with these guarantees:

1. ✅ Badge hides **immediately** when you open a chat
2. ✅ Badge shows **backend's count** when you're not viewing
3. ✅ Badge appears **immediately** when new messages arrive
4. ✅ Badge hides **immediately** when you open that chat
5. ✅ No 3-second delays or grace periods

**Trust the backend's `unreadCount` — that's the source of truth!**

