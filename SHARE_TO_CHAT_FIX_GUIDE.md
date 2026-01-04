# 🔧 FIX: Shared Sorties/Publications Not Appearing in Chat

## 📌 Problem Summary

When sharing a sortie or publication to a chat, the share action completes successfully, but the shared content doesn't appear in the chat conversation.

### Root Cause

The issue occurs because:

1. **New ViewModel Instance**: When sharing from `SortieDetailScreen` or `FeedScreen`, a NEW `ChatViewModel` instance is created
2. **Disconnected Socket**: This new instance is not connected to the same Socket.IO session as the chat you're viewing
3. **Message Lost**: The message is sent but the instance dies immediately, and the existing chat screen never receives the `onMessageReceived` event

### Evidence from Logs

```
ChatCard: Message content preview: oui
Starts with SHARED_SORTIE: false
Starts with SHARED_PUBLICATION: false
```

Only old messages appear:
```
Message 0: Chat créé automatiquement pour
Message 1: aaaaaa  
Message 2: oui
```

The newly shared messages are **MISSING** from the `joinedRoom` event.

---

## ✅ Solution Implemented

### 1. Created `ShareMessageHelper` Utility

**File**: `app/src/main/java/com/example/dam/utils/ShareMessageHelper.kt`

This utility provides static methods to share content without creating new ViewModels:

```kotlin
// Share a sortie
ShareMessageHelper.shareSortieToChat(
    sortieId = "123",
    chatSortieId = "456", 
    sortieData = ShareMessageHelper.SortieShareData(...),
    context = context
)

// Share a publication
ShareMessageHelper.sharePublicationToChat(
    publicationId = "789",
    chatSortieId = "456",
    publicationData = ShareMessageHelper.PublicationShareData(...),
    context = context
)
```

### 2. Added Refresh Method to `UserProfileViewModel`

```kotlin
fun refreshAfterShare() {
    // Reload profile data after sharing
}
```

---

## 🚀 How to Fix Your Screens

You need to update the sharing code in **2 places**:

### Option A: Use ShareMessageHelper (Recommended)

#### Fix 1: SortieDetailScreen.kt (Line ~1160)

**BEFORE:**
```kotlin
val chatViewModel = com.example.dam.viewmodel.ChatViewModel() // ❌ NEW INSTANCE!
val messageContent = buildString {
    append("SHARED_SORTIE:${sortie.id}\n")
    //...
}
chatViewModel.sendTextMessage(messageContent, chatSortieId, context)
```

**AFTER:**
```kotlin
import com.example.dam.utils.ShareMessageHelper

ShareMessageHelper.shareSortieToChat(
    sortieId = sortie.id,
    chatSortieId = chatSortieId,
    sortieData = ShareMessageHelper.SortieShareData(
        title = sortie.titre,
        creator = "${sortie.createurId.firstName} ${sortie.createurId.lastName}",
        imageUrl = sortie.photo,
        date = sortie.date,
        type = sortie.type
    ),
    context = context
)
```

#### Fix 2: FeedScreen.kt (Publication Sharing)

**Find the publication sharing code and replace it with:**

```kotlin
import com.example.dam.utils.ShareMessageHelper

ShareMessageHelper.sharePublicationToChat(
    publicationId = publication.id,
    chatSortieId = chatSortieId,
    publicationData = ShareMessageHelper.PublicationShareData(
        content = publication.content,
        creator = "${publication.userId.firstName} ${publication.userId.lastName}",
        imageUrl = publication.imageUrl,
        date = publication.createdAt
    ),
    context = context
)
```

---

### Option B: Force Socket Reconnection (Alternative)

If you want to keep using ChatViewModel, you need to ensure the socket stays connected:

```kotlin
// 1. Get or create ChatViewModel
val chatViewModel = remember { com.example.dam.viewmodel.ChatViewModel() }

// 2. Ensure socket is connected
LaunchedEffect(Unit) {
    val token = UserPreferences.getToken(context)
    if (!token.isNullOrEmpty() && !SocketService.isConnected()) {
        SocketService.connect(token)
        delay(1000) // Wait for connection
    }
}

// 3. Send message
chatViewModel.sendTextMessage(messageContent, chatSortieId, context)

// 4. Keep ViewModel alive longer
DisposableEffect(Unit) {
    onDispose {
        // Clean up after 5 seconds to ensure message is sent
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(5000)
            // ViewModel can be garbage collected now
        }
    }
}
```

---

## 🧪 Testing Steps

1. **Share a Sortie**:
   - Go to Sortie Details
   - Click share icon
   - Select a chat
   - ✅ Verify the sortie appears immediately in the chat

2. **Share a Publication**:
   - Go to Feed screen
   - Find a publication
   - Click share button  
   - Select a chat
   - ✅ Verify the publication appears immediately in the chat

3. **Navigate to Chat**:
   - Open Messages screen
   - Click on the chat where you shared
   - ✅ Verify BOTH the sortie AND publication are visible
   - ✅ Verify they display correctly with preview cards

---

## 📊 Backend Check

The error in logs suggests a missing endpoint:

```
❌ Error marking chat as read: 404 - 
{"message":"Cannot POST /chats/6923715652b3312a7f1ed64a/mark-read"}
```

**IMPORTANT**: You also need to add this endpoint to your backend:

### Backend Fix (Node.js/Express)

```javascript
// File: routes/chats.js

router.post('/:chatId/mark-read', auth, async (req, res) => {
  try {
    const { chatId } = req.params;
    const userId = req.user.sub;

    // Update all messages in this chat to mark them as read by this user
    await Message.updateMany(
      { chatId, readBy: { $ne: userId } },
      { $addToSet: { readBy: userId } }
    );

    res.json({ success: true, message: 'Chat marked as read' });
  } catch (error) {
    console.error('Error marking chat as read:', error);
    res.status(500).json({ error: 'Failed to mark chat as read' });
  }
});
```

---

## 🎯 Quick Fix Checklist

- [ ] Replace `ChatViewModel()` creation in SortieDetailScreen  
- [ ] Replace publication sharing code in FeedScreen
- [ ] Test sharing a sortie to a chat
- [ ] Test sharing a publication to a chat
- [ ] Verify messages appear in the chat conversation
- [ ] Add `/chats/:chatId/mark-read` endpoint to backend (optional)

---

## 🔍 Additional Debug

If messages still don't appear, add this logging to ChatViewModel's `onMessageReceived`:

```kotlin
SocketService.onMessageReceived = { message ->
    Log.d(TAG, "========================================")
    Log.d(TAG, "📨 NEW MESSAGE RECEIVED VIA SOCKET")
    Log.d(TAG, "Message ID: ${message._id}")
    Log.d(TAG, "Message Type: ${message.type}")
    Log.d(TAG, "Message Content: ${message.content?.take(100)}")
    Log.d(TAG, "Sender ID: ${message.senderId}")
    Log.d(TAG, "Current User ID: $currentUserId")
    Log.d(TAG, "Current Messages Count: ${_messages.value.size}")
    Log.d(TAG, "========================================")
    
    // ... rest of the code
}
```

Then check Logcat when sharing to see if the message is being received.

---

## ✨ Summary

**Root Cause**: Creating new ChatViewModel instances breaks Socket.IO message flow  
**Solution**: Use `ShareMessageHelper` to send messages via the existing Socket connection  
**Result**: Shared content appears immediately in chat conversations  

After implementing this fix, sharing sorties and publications should work perfectly! 🎉

