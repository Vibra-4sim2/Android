# 🧪 Testing Guide: Badge Reappear Fix

## Quick Test: Verify Badge Shows Up When New Messages Arrive

### Prerequisites
- Have at least 2 devices/accounts to send messages between
- Or use backend API to simulate incoming messages

---

## Test Scenario 1: Badge Disappears and Reappears ✅

### Steps:

1. **Initial State**
   - Open app on Device A (your main device)
   - Go to Messages tab
   - You should see chat groups listed

2. **Receive Message**
   - Send a message from Device B to a group chat
   - **Expected:** Badge appears on Device A with count "1" (or higher)
   - **Look for:** Red circular badge next to chat name

3. **Open Chat and Read**
   - On Device A: Tap the chat group
   - **Expected:** Badge disappears immediately (optimistic UI)
   - Read the messages

4. **Close Chat**
   - Press back to return to messages list
   - **Expected:** Badge stays hidden (messages were read)

5. **🔥 CRITICAL TEST: Send New Message**
   - Send another message from Device B
   - Wait ~2 seconds for backend to sync
   - **Expected:** Badge REAPPEARS on Device A! ✅
   - **Badge should show:** "1" (for the new unread message)

6. **Verify Badge Persists**
   - Close app completely on Device A
   - Reopen app
   - Go to Messages tab
   - **Expected:** Badge still shows "1"

---

## Test Scenario 2: Multiple New Messages

### Steps:

1. Open chat, read messages, go back (badge hidden)
2. Send 3 messages from Device B
3. **Expected:** Badge shows "3" on Device A
4. Open chat again
5. **Expected:** Badge disappears immediately
6. Go back
7. **Expected:** Badge stays hidden (all read)

---

## Test Scenario 3: Rapid Open/Close

### Steps:

1. Send message from Device B
2. On Device A: Quickly tap chat (just 1 second) and immediately go back
3. Send another message from Device B
4. **Expected:** Badge appears with correct count

---

## 📊 What to Look For

### ✅ Success Indicators:
- Badge disappears immediately when opening chat
- Badge stays hidden after reading all messages
- **Badge REAPPEARS when new message arrives** ← Main fix!
- Badge count is accurate
- Works after app restart

### ❌ Failure Indicators:
- Badge doesn't show up when new message arrives
- Badge shows wrong count
- Badge appears/disappears erratically

---

## 🔍 Debug Logs to Monitor

Enable logcat filtering with tag: `GroupChatItem`

Look for these logs:

```
📊 Badge State for [Group Name]
   sortieId: [ID]
   unreadCount (backend): 1
   isOptimistic: true
   effectiveCount: 1

🆕 NEW MESSAGES detected, clearing optimistic state to show badge
```

When you see "🆕 NEW MESSAGES detected", it means the fix is working correctly!

---

## 🎯 Expected Behavior Summary

| Situation | Badge State |
|-----------|------------|
| New message arrives | Badge shows ✅ |
| User opens chat | Badge disappears immediately ✅ |
| User reads messages | Badge stays hidden ✅ |
| New message after reading | Badge REAPPEARS ✅ (Fixed!) |
| App restart with unread | Badge persists ✅ |

---

## ⚡ Quick Visual Test

**Before Fix:**
```
1. See badge (3) → Open chat → Badge gone
2. Close chat → Badge still gone
3. New message arrives → Badge STILL GONE ❌
```

**After Fix:**
```
1. See badge (3) → Open chat → Badge gone
2. Close chat → Badge still gone
3. New message arrives → Badge shows (1) ✅
```

---

## 🐛 If Badge Doesn't Reappear

Check:
1. Backend is updating `unreadCount` correctly
2. `ChatStateManager.initialize()` is called
3. Messages are being fetched after new message arrives
4. WebSocket connection is active
5. Check logs for "NEW MESSAGES detected" message

---

## ✨ Success!

If badges show up correctly when new messages arrive after reading, the fix is working! 🎉

The badge system now behaves exactly like WhatsApp/Messenger/Telegram.

