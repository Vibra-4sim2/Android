# Share Message Debug Analysis

## Problem
Shared sorties and publications are being sent to chat but not appearing as cards in the chat UI.

## What Works ✅
1. **Message sending**: Messages with `SHARED_SORTIE:` and `SHARED_PUBLICATION:` prefixes are being sent successfully
2. **Database storage**: Messages are being saved to the database via MessageRepository
3. **WebSocket reception**: Messages are being received back via WebSocket
4. **Message conversion**: Messages are being converted to MessageUI with content intact

## Current Implementation

### 1. Sending Shared Messages (ChatMessageSender.kt)
```kotlin
// For sortie:
content = "SHARED_SORTIE:$sortieId"

// For publication:
content = "SHARED_PUBLICATION:$publicationId\nAUTHOR:...\nCONTENT:...\nIMAGE:...\nDATE:..."
```

### 2. Message Reception (ChatViewModel.kt)
- Messages received via WebSocket `onMessageReceived`
- Converted to MessageUI via `toMessageUI()` function
- Added to `_messages` StateFlow

### 3. UI Rendering (ChatConversationScreen.kt, lines 1189-1219)
```kotlin
val isSharedSortie = message.content?.startsWith("SHARED_SORTIE:") == true
val isSharedPublication = message.content?.startsWith("SHARED_PUBLICATION:") == true

if (isSharedSortie && message.content != null) {
    SharedSortieCard(...)
}

if (isSharedPublication && message.content != null) {
    SharedPublicationCard(...)
}
```

## Potential Issues 🔍

### Issue 1: Message Content Modification
The `content` field might be getting modified during:
- Database storage
- WebSocket transmission
- MessageUI conversion

### Issue 2: UI Rendering Condition
The cards have conditions that need to be met:
```kotlin
!message.content.isNullOrEmpty() && 
message.type != MessageType.AUDIO && 
!isSharedSortie && 
!isSharedPublication
```

This means if the message is correctly identified as shared, the text content should NOT be displayed.

### Issue 3: Logging Evidence
From the attached logs, we can see:
```
D/ChatCard: ========================================
D/ChatCard: Message ID: ...
D/ChatCard: Message content preview: SHARED_SORTIE:...
D/ChatCard: Starts with SHARED_SORTIE: true
D/ChatCard: ========================================
```

This shows the detection IS working!

## Next Steps

Need to verify:
1. ✅ Are the SharedSortieCard and SharedPublicationCard components actually being called?
2. ✅ Are there any layout/rendering issues preventing the cards from showing?
3. ✅ Are there any errors in the card components themselves?

## Solution Direction

The issue is likely in the card components or how they're positioned in the layout. Need to:
1. Add more detailed logging in SharedSortieCard and SharedPublicationCard
2. Check if the cards are being composed but not visible
3. Verify the card layout and constraints

