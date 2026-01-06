# 🧪 Quick Test Guide - Badge Fix

## Before Testing
1. **Build and run the app** on your device/emulator
2. Make sure you have at least 2 chats with unread messages

## Test Steps

### ✅ Test 1: Badge Disappears When Opening Chat

**Steps:**
1. Open the **Messages** screen
2. Look for a chat with a badge (red circle with number)
3. **Note the badge count** (e.g., "1")
4. **Click on that chat** to open the conversation

**Expected Result:**
- ✅ Chat screen opens
- ✅ When you go back to Messages screen, the **badge is gone** (or count is 0)

**Logcat to watch:**
```
GroupChatItem: [sortieId] 🚫 Badge=0 (currently viewing)
```

---

### ✅ Test 2: Badge Stays Hidden After Returning

**Steps:**
1. After Test 1, you're back on the Messages screen
2. The chat you just viewed should have **no badge**
3. **Don't send new messages** - just wait 5 seconds
4. **Refresh** by pulling down on the list

**Expected Result:**
- ✅ Badge **remains hidden** (no new messages)
- ✅ Chat still appears in the list normally

**Logcat to watch:**
```
GroupChatItem: [sortieId] 📊 Badge=0 (from backend)
```

---

### ✅ Test 3: Badge Appears for New Messages

**Steps:**
1. Use **another phone/account** to send a message to the chat
2. On your test device, **refresh** the Messages screen (pull down)

**Expected Result:**
- ✅ Badge **reappears** with count "1"
- ✅ Message preview shows the new message

**Logcat to watch:**
```
GroupChatItem: [sortieId] 📊 Badge=1 (from backend)
```

---

### ✅ Test 4: Multiple Chats Work Correctly

**Steps:**
1. Open **Chat A** → Badge disappears
2. Return to Messages
3. Open **Chat B** → Badge disappears
4. Return to Messages
5. Check both Chat A and Chat B

**Expected Result:**
- ✅ Both badges should be **hidden** (assuming no new messages arrived)
- ✅ Other chats (not opened) still show their badges

---

## Debugging Tips

### If Badge Doesn't Disappear:

**Check Logcat for:**
```bash
adb logcat | grep -E "(GroupChatItem|ChatStateManager|ChatViewModel)"
```

**Look for:**
- `ChatStateManager: ✅ Marking chat as opened: [sortieId]`
- `GroupChatItem: [sortieId] 🚫 Badge=0 (currently viewing)`

**If not found:**
- The `markChatAsOpened` might not be called
- Check that `ChatViewModel.connectAndJoinRoom` is executing

---

### If Badge Reappears Immediately After Closing:

**Check Logcat for:**
```bash
adb logcat | grep -E "(ChatConversationScreen|clearOptimisticState)"
```

**Look for:**
- `ChatConversationScreen: 🚪 DisposableEffect onDispose APPELÉ`
- `ChatStateManager: 🧹 Clearing chat from viewing: [sortieId]`

**Possible Issues:**
- The DisposableEffect might not be called (navigation issue)
- Check that you're using `navController.popBackStack()` to go back

---

### If Badge Never Shows Up (Even for New Messages):

**Check Backend Response:**
```bash
adb logcat | grep -E "(ChatRepository|okhttp)"
```

**Look for the JSON response** containing:
```json
"lastMessage": {
  "readBy": [...],  // Should NOT contain your userId for new messages
  ...
}
"unreadCount": 1  // Should be present
```

**Possible Issues:**
- Backend always returns `"unreadCount": null`
- Backend always includes your userId in `readBy` even for new messages
- Backend needs to be fixed (see BADGE_FIX_RESTORED.md)

---

## Success Criteria

✅ **All tests pass** if:
1. Badge disappears when opening a chat
2. Badge stays hidden when returning (no new messages)
3. Badge reappears when new messages arrive
4. Works correctly for multiple chats

## Next Steps

If tests fail, please:
1. Copy the **relevant logcat output** (last 50 lines)
2. Note which **specific test failed**
3. Share the logs so we can diagnose

## Quick Logcat Command

```bash
# Windows (PowerShell)
adb logcat | Select-String -Pattern "GroupChatItem|ChatStateManager|ChatViewModel"

# Mac/Linux
adb logcat | grep -E "(GroupChatItem|ChatStateManager|ChatViewModel)"
```

This will show only the relevant logs for badge debugging.

