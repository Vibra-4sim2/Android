# 🎯 Badge Persistence Issue - COMPLETE FIX

## ❌ Problem Description

The unread message badges in the discussions list were not behaving correctly:

1. **Badge doesn't disappear when viewing messages**: User opens a chat, reads messages, goes back → badge still shows
2. **Badge reappears after leaving chat**: User reads all messages, leaves chat, returns to list → badge mysteriously reappears even though messages were already read

## 🔍 Root Cause Analysis

The issue was caused by a **race condition** between:

1. **Frontend optimistic UI** (immediately hiding badge when user opens chat)
2. **Backend sync delay** (WebSocket marks messages as read, but backend DB takes time to update)
3. **List refresh** (fetches data from backend before it's fully synced)

### The Race Condition Flow

```
User opens chat
    ↓
Frontend: markChatAsOpened() → badge hides (optimistic UI)
    ↓
Frontend: markAllMessagesAsRead() via WebSocket
    ↓
User leaves chat (optimistic flag cleared)
    ↓
List refreshes immediately
    ↓
Backend: Still processing "mark as read" requests
    ↓
Backend returns: unreadCount = 5 (OLD DATA - not yet updated)
    ↓
Frontend: "Oh, unreadCount > 0, must be NEW messages!" → BADGE REAPPEARS ❌
    ↓
5 seconds later...
    ↓
Backend finally updates: unreadCount = 0
    ↓
List refreshes again → badge disappears
```

### Why the Previous Logic Failed

```kotlin
// ❌ OLD LOGIC (BROKEN)
lastSeenCount == 0 -> {
    if (group.unreadCount > 0) {
        // Assumes ANY unreadCount > 0 means NEW messages
        // But backend might still return OLD unread count!
        group.unreadCount  // ← SHOWS BADGE TOO EARLY
    } else {
        0
    }
}
```

The problem: We couldn't distinguish between:
- **Stale backend data** (backend hasn't synced yet, still shows old unreadCount)
- **Genuine new messages** (new messages arrived after user left)

## ✅ Solution: Timestamp-Based Grace Period

We added a **30-second grace period** after marking messages as read. During this period, we ignore any `unreadCount > 0` from the backend, assuming it's stale data that hasn't synced yet.

### New Data Structure

```kotlin
// Added to ChatStateManager
private val _readTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())
val readTimestamps: StateFlow<Map<String, Long>> = _readTimestamps.asStateFlow()
```

### Key Changes

#### 1. Record Timestamp When Messages Are Read

```kotlin
// ChatStateManager.kt - updateLastSeenCount()
fun updateLastSeenCount(sortieId: String, count: Int) {
    _lastSeenUnreadCounts.value = _lastSeenUnreadCounts.value + (sortieId to count)
    
    // ✅ NEW: Record timestamp when all messages are read
    if (count == 0) {
        _readTimestamps.value = _readTimestamps.value + (sortieId to System.currentTimeMillis())
    }
    
    savePersistedState()
}
```

#### 2. Use Timestamp to Ignore Stale Backend Data

```kotlin
// MessagesListScreen.kt - GroupChatItem badge logic
lastSeenCount == 0 -> {
    val now = System.currentTimeMillis()
    val timeSinceRead = now - readTimestamp
    val syncGracePeriod = 30_000L // 30 seconds
    
    if (timeSinceRead < syncGracePeriod) {
        // Within grace period - hide badge even if backend shows unread
        0  // ✅ PREVENTS BADGE REAPPEARING
    } else if (group.unreadCount > 0) {
        // After grace period with unread count - must be NEW messages
        group.unreadCount  // ✅ SHOWS BADGE FOR REAL NEW MESSAGES
    } else {
        0
    }
}
```

#### 3. Persist Timestamps Across App Restarts

```kotlin
// ChatStateManager.kt - savePersistedState()
val timestampsJson = org.json.JSONObject()
_readTimestamps.value.forEach { (key, value) ->
    timestampsJson.put(key, value)
}
editor.putString(KEY_READ_TIMESTAMPS, timestampsJson.toString())
```

## 🎬 Complete User Flow (Fixed)

### Scenario 1: User Reads Messages and Leaves

```
1. User opens chat
   → markChatAsOpened() called
   → Badge hides immediately (optimistic UI)

2. Messages are marked as read
   → markAllMessagesAsRead() via WebSocket
   → updateLastSeenCount(sortieId, 0) called
   → Timestamp recorded: readTimestamps[sortieId] = 1640000000000

3. User leaves chat
   → clearOptimisticState() called
   → Optimistic flag cleared, but timestamp persists

4. List refreshes immediately (0.5s later)
   → Backend still shows unreadCount = 5 (stale)
   → Time since read: 500ms
   → 500ms < 30,000ms (grace period)
   → Badge stays HIDDEN ✅

5. List refreshes again (10s later)
   → Backend now shows unreadCount = 0 (synced)
   → Badge stays HIDDEN ✅

6. NEW message arrives (60s later)
   → Backend shows unreadCount = 1
   → Time since read: 60,000ms
   → 60,000ms > 30,000ms (past grace period)
   → Badge SHOWS UP ✅
```

### Scenario 2: User Currently Viewing Chat

```
1. User opens chat
   → recentlyOpenedChats.contains(sortieId) = true
   → Badge hides immediately
   → effectiveUnreadCount = 0 ✅

2. New message arrives while viewing
   → Backend updates unreadCount = 1
   → But still isCurrentlyViewing = true
   → Badge stays HIDDEN ✅

3. User leaves chat
   → recentlyOpenedChats cleared
   → If messages were read: timestamp set, badge stays hidden
   → If messages NOT read: no timestamp, badge shows up
```

## 📊 State Management

### ChatStateManager Stores 3 Pieces of Data

1. **recentlyOpenedChats: Set&lt;String&gt;**
   - Tracks chats currently being viewed
   - Used for optimistic badge hiding
   - Cleared when user leaves chat

2. **lastSeenUnreadCounts: Map&lt;String, Int&gt;**
   - Last known unread count when messages were read
   - 0 = all messages were read
   - -1 = chat never opened
   - Persists across sessions

3. **readTimestamps: Map&lt;String, Long&gt;** ← NEW!
   - Timestamp (milliseconds) when messages were marked as read
   - Used to calculate grace period
   - Persists across sessions

### Persistence

All data is saved to SharedPreferences as JSON:

```json
{
  "recently_opened_chats": ["sortie123", "sortie456"],
  "last_seen_counts": {
    "sortie123": 0,
    "sortie456": 0
  },
  "read_timestamps": {
    "sortie123": 1640000000000,
    "sortie456": 1640000100000
  }
}
```

## 🔧 Files Modified

1. **ChatStateManager.kt**
   - Added `_readTimestamps` StateFlow
   - Updated `loadPersistedState()` to load timestamps
   - Updated `savePersistedState()` to save timestamps
   - Updated `updateLastSeenCount()` to record timestamp when count = 0

2. **MessagesListScreen.kt**
   - Updated `GroupChatItem` badge logic
   - Added 30-second grace period check
   - Improved logging for debugging

## 🧪 Testing Checklist

- [x] Open chat → badge hides immediately
- [x] Read messages → badge stays hidden when returning to list
- [x] Wait 1 second after reading → badge still hidden (within grace period)
- [x] Wait 10 seconds after reading → badge still hidden (within grace period)
- [x] Wait 40 seconds after reading → badge still hidden (no new messages)
- [x] New message arrives → badge shows up correctly
- [x] Close app → reopen → badge state persists correctly
- [x] Multiple chats → each has independent badge state

## 🎯 Expected Behavior

### ✅ Badge SHOULD Hide When:
1. User is currently viewing the chat
2. User has read all messages (within 30s grace period)
3. User has read all messages and backend has synced (after grace period)

### ✅ Badge SHOULD Show When:
1. New unread messages arrive (after grace period)
2. User has never opened the chat and messages exist
3. User opened chat but didn't read messages (backend returns unread > 0 after grace period)

## 🐛 Debugging

Enable detailed logs:
```
adb logcat | grep -E "ChatStateManager|GroupChatItem|ChatViewModel"
```

Key log messages:
- `📖 Marquage de X messages comme lus` - Messages being marked as read
- `⏰ Recorded read timestamp` - Timestamp recorded
- `🚫 Badge=0 (grace period: Xms < 30000ms)` - Grace period active
- `📊 Badge=X (NEW messages after grace period)` - New messages detected

## 📝 Summary

The fix introduces a **timestamp-based grace period** that prevents badges from reappearing due to backend sync delays. This provides a smooth, predictable user experience while maintaining data integrity.

**Key Insight**: We can't rely solely on comparing unread counts because of race conditions. Instead, we use **time elapsed since messages were read** to distinguish between stale backend data and genuine new messages.

