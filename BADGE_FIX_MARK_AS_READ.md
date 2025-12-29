# 🔧 Badge Fix: Mark Messages as Read

## Problem
The badges were not disappearing when you opened a chat and read the messages. The issue was that:
1. The badge hiding logic (based on `ChatStateManager`) was working
2. BUT the backend was NOT marking messages as read
3. So when you went back to messages list, the badges reappeared because the backend still reported unread messages

## Root Cause
Looking at the logs:
```
"readBy":[]
```
All messages had empty `readBy` arrays, meaning NO messages were being marked as read on the backend.

The `markAllMessagesAsRead()` function in ChatViewModel was **private** and **never called** when entering a chat.

## Solution

### 1. Made `markAllMessagesAsRead()` Public
**File**: `ChatViewModel.kt`

Changed the function from `private suspend fun markAllMessagesAsRead()` to `public fun markAllMessagesAsRead(sortieId: String, context: Context)` so it can be called from the ChatConversationScreen.

The function now:
- Accepts sortieId and context as parameters
- Runs in viewModelScope (no need for suspend)
- Fetches chatId from backend if not available
- Calls backend API to mark chat as read
- Also sends WebSocket events for each message

### 2. Call Mark As Read When Entering Chat
**File**: `ChatConversationScreen.kt`

Added call to mark messages as read immediately when entering chat:
```kotlin
LaunchedEffect(sortieId) {
    // ... existing code ...
    
    // ✅ IMPORTANT: Mark messages as read immediately when entering chat
    android.util.Log.d("ChatConversationScreen", "📖 Marking all messages as read on entry...")
    viewModel.markAllMessagesAsRead(sortieId, context)
}
```

### 3. Keep Existing Mechanisms
The existing code already had:
- ✅ `ChatStateManager.markChatAsOpened(sortieId)` - Hides badge while viewing
- ✅ `forceMarkAllAsReadSync()` on exit - Ensures backend is updated when leaving
- ✅ `MessagesListScreen` auto-refresh on resume with 800ms delay

## How It Works Now

### When Opening a Chat:
1. **Instant badge hide**: `ChatStateManager.markChatAsOpened(sortieId)` ✅
2. **Backend update**: `viewModel.markAllMessagesAsRead(sortieId, context)` ✅
3. **WebSocket update**: Individual messages marked as read ✅

### When Leaving a Chat:
1. **Force backend sync**: `forceMarkAllAsReadSync()` ✅
2. **Clear viewing state**: `ChatStateManager.clearOptimisticState(sortieId)` ✅

### When Returning to Messages List:
1. **Wait 800ms**: Give backend time to process ✅
2. **Refresh chat list**: Load updated unread counts from backend ✅
3. **Badges updated**: Show 0 if messages were read ✅

## Expected Behavior

1. **Open chat** → Badge disappears immediately (ChatStateManager)
2. **Read messages** → Backend marks as read (API + WebSocket)
3. **Go back** → Badge stays hidden (backend has unreadCount=0)
4. **New message arrives** → Badge appears again
5. **Open chat again** → Badge disappears, cycle repeats

## Testing Steps

1. Have someone send you a message in a chat
2. See the badge appear on messages list (badge count = 1)
3. Open the chat
4. Badge should disappear IMMEDIATELY
5. Go back to messages list
6. Badge should STAY hidden (not reappear)
7. Have someone send another message
8. Badge should appear again with new count

## Files Modified

1. ✅ `ChatViewModel.kt` - Made `markAllMessagesAsRead()` public
2. ✅ `ChatConversationScreen.kt` - Added call to mark as read on entry

## No Changes Needed

These files already had the right logic:
- ✅ `ChatStateManager.kt` - Session-based viewing tracking
- ✅ `MessagesListScreen.kt` - Auto-refresh on resume
- ✅ `ChatRepository.kt` - API call to mark chat as read
- ✅ `GroupChatItem.kt` - Badge display logic

---

**Date**: December 28, 2025
**Status**: ✅ FIXED - Badges now properly hide and stay hidden after reading messages

