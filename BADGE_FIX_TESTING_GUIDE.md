# 🧪 Badge Fix Testing Guide

**Date:** December 27, 2025  
**Purpose:** Step-by-step guide to test the badge persistence fix

---

## 📋 Pre-Testing Checklist

Before you begin testing:

- [ ] App is compiled and installed on device/emulator
- [ ] You have at least 2 user accounts (User A and User B)
- [ ] Users are part of at least one shared discussion
- [ ] Logcat is open and filtered to: `GroupChatItem|MessagesListScreen|ChatStateManager`

---

## 🧪 Test Suite

### **Test 1: Basic Badge Disappearance**

**Purpose:** Verify that badges disappear when messages are read

**Steps:**
1. Login as **User A**
2. Send a message in a discussion: "Test message 1"
3. Logout and login as **User B**
4. Navigate to Messages screen
5. **✅ VERIFY:** Red badge showing "1" appears on the discussion
6. Tap on the discussion to open it
7. **✅ VERIFY:** Badge disappears **immediately** (optimistic update)
8. View the message
9. Wait 2-3 seconds
10. Press back button to return to Messages list
11. **✅ VERIFY:** Badge stays hidden (does not reappear)
12. Wait 30 seconds
13. **✅ VERIFY:** Badge still hidden

**Expected Result:** Badge disappears immediately when opening chat and stays hidden permanently

**Logcat to check:**
```
GroupChatItem: 📊 Badge State for [Chat Name] (sortieId):
GroupChatItem:    unreadCount (from backend): 1
GroupChatItem:    isOptimisticallyRead: true
GroupChatItem:    effectiveUnreadCount (displayed): 0
GroupChatItem: ⏳ Optimistic state ACTIVE

[After backend confirms]
GroupChatItem: ✅ Backend confirmed read (unreadCount=0)
```

---

### **Test 2: Multiple Unread Messages**

**Purpose:** Verify badge behavior with multiple unread messages

**Steps:**
1. Login as **User A**
2. Send 5 messages rapidly in a discussion
3. Logout and login as **User B**
4. Navigate to Messages screen
5. **✅ VERIFY:** Red badge showing "1" (based on last message)
6. Tap discussion to open
7. **✅ VERIFY:** Badge disappears immediately
8. Scroll through all 5 messages
9. Press back
10. **✅ VERIFY:** Badge stays hidden

**Expected Result:** Badge disappears and stays hidden even with multiple unread messages

---

### **Test 3: Multiple Discussions**

**Purpose:** Test badge behavior across multiple discussions

**Steps:**
1. Have **User A** send a message in 3 different discussions
2. Login as **User B**
3. Navigate to Messages screen
4. **✅ VERIFY:** All 3 discussions show red badge "1"
5. Open Discussion 1 → Badge disappears → Press back
6. **✅ VERIFY:** Discussion 1 has no badge, 2 & 3 still have badges
7. Open Discussion 2 → Badge disappears → Press back
8. **✅ VERIFY:** Discussion 1 & 2 have no badges, 3 still has badge
9. Open Discussion 3 → Badge disappears → Press back
10. **✅ VERIFY:** All 3 discussions have no badges

**Expected Result:** Each discussion's badge is independent and clears correctly

---

### **Test 4: Fast Navigation (Edge Case)**

**Purpose:** Test badge behavior when quickly navigating

**Steps:**
1. Have **User A** send a message
2. Login as **User B**, go to Messages
3. **✅ VERIFY:** Badge shows "1"
4. Tap discussion (badge disappears)
5. **Immediately** press back (within 1 second)
6. **✅ VERIFY:** Badge is hidden (optimistic)
7. Wait 5 seconds
8. **✅ VERIFY:** Badge stays hidden

**Expected Result:** Optimistic state handles fast navigation correctly

---

### **Test 5: New Message While Viewing**

**Purpose:** Test real-time message reception

**Steps:**
1. Login as **User B** first
2. Open a discussion (leave it open)
3. On another device, login as **User A**
4. Send a new message to the same discussion
5. On **User B**'s device:
   - **✅ VERIFY:** New message appears in chat
6. Press back to Messages list
7. **✅ VERIFY:** No badge appears (message was already read)

**Expected Result:** Real-time messages are marked as read automatically when viewing

---

### **Test 6: Refresh Cycle Observation**

**Purpose:** Observe the refresh strategy in action

**Steps:**
1. Have **User A** send a message
2. Login as **User B**, go to Messages, see badge
3. Open discussion (badge disappears)
4. Press back
5. Watch Logcat for refresh cycle:

**Expected Logcat:**
```
MessagesListScreen: 🔄 Refresh #1: Immediate (0ms)
MessagesListScreen: 🔄 Refresh #2: After 300ms
MessagesListScreen: 🔄 Refresh #3: After 1s
MessagesListScreen: 🔄 Refresh #4: After 2.5s
MessagesListScreen: 🔄 Refresh #5: After 5s
MessagesListScreen: 🔄 Refresh #6: After 10s
MessagesListScreen: 🔄 Refresh #7 (FINAL): After 20s
MessagesListScreen: ✅ Refresh cycle complete
```

**Expected Result:** See 7 refreshes over 20 seconds in Logcat

---

### **Test 7: Slow Backend Simulation**

**Purpose:** Test optimistic state with slow backend

**Steps:**
1. Have **User A** send a message
2. Login as **User B**, go to Messages
3. Open discussion with unread message
4. **Immediately after opening**, turn OFF WiFi/mobile data
5. **✅ VERIFY:** Badge is hidden (optimistic)
6. Wait 5 seconds, turn WiFi back ON
7. **✅ VERIFY:** Badge stays hidden (backend catches up)

**Expected Result:** Optimistic state persists until backend confirms

---

### **Test 8: Timeout Safety Mechanism**

**Purpose:** Test the 30-second timeout fallback

**Steps:**
1. This test requires **backend to be down** or **network issues**
2. Have **User A** send a message (while backend is working)
3. Bring backend DOWN or disconnect network
4. Login as **User B**, see badge
5. Open discussion (badge disappears optimistically)
6. **Keep backend down**
7. Press back
8. Wait 30 seconds
9. **✅ VERIFY:** Badge reappears after ~30 seconds

**Expected Logcat:**
```
GroupChatItem: ⏳ Optimistic state ACTIVE - waiting for backend
[30 seconds pass]
GroupChatItem: ⚠️ 30s timeout: Clearing optimistic state
```

**Expected Result:** Safety timeout prevents permanent badge hiding

---

### **Test 9: App Restart**

**Purpose:** Verify badge state persists across app restarts

**Steps:**
1. Have **User A** send a message
2. Login as **User B**, see badge
3. Open discussion, view message, press back
4. **✅ VERIFY:** Badge is hidden
5. Close app completely (swipe from recents)
6. Reopen app
7. Navigate to Messages screen
8. **✅ VERIFY:** Badge is still hidden (backend state persisted)

**Expected Result:** Badge state is consistent after app restart

---

### **Test 10: Concurrent Messages**

**Purpose:** Test badge with rapid incoming messages

**Steps:**
1. Login as **User B** on device 1
2. Go to Messages screen (don't open any discussion)
3. Login as **User A** on device 2
4. Send 10 messages rapidly to a discussion
5. On device 1 (User B):
   - **✅ VERIFY:** Badge updates (may show "1" for last message)
6. Open the discussion
7. **✅ VERIFY:** Badge disappears immediately
8. Scroll through all messages
9. Press back
10. **✅ VERIFY:** Badge stays hidden

**Expected Result:** Badge handles concurrent messages correctly

---

## 🐛 Troubleshooting Tests

### **If badge doesn't disappear:**

1. Check Logcat for errors
2. Verify WebSocket connection:
   ```
   SocketService: ✅ Socket connected
   ChatViewModel: 🏠 EVENT: joinedRoom
   ```
3. Check if `markChatAsOpened` was called:
   ```
   ChatStateManager: ✅ Chat marked as opened (optimistic): sortieId
   ```
4. Verify token is valid

### **If badge reappears incorrectly:**

1. Check if truly a new message or same message
2. Check backend logs for `markAsRead` processing
3. Verify WebSocket `messageRead` events:
   ```
   SocketService: 👁️ Message read: messageId by userId
   ChatViewModel: 📖 Message marked as read: messageId
   ```
4. Check optimistic state:
   ```
   GroupChatItem: isOptimisticallyRead: true
   ```

---

## ✅ Success Criteria

All tests should pass with these results:

| Test | Expected Result | Pass/Fail |
|------|----------------|-----------|
| Test 1: Basic | Badge disappears and stays hidden | ✅ |
| Test 2: Multiple messages | Badge clears correctly | ✅ |
| Test 3: Multiple discussions | Independent badge clearing | ✅ |
| Test 4: Fast navigation | Optimistic state works | ✅ |
| Test 5: Real-time | New messages handled | ✅ |
| Test 6: Refresh cycle | 7 refreshes visible | ✅ |
| Test 7: Slow backend | Optimistic state persists | ✅ |
| Test 8: Timeout | Safety mechanism works | ✅ |
| Test 9: App restart | State persists | ✅ |
| Test 10: Concurrent | Handles rapid messages | ✅ |

---

## 📊 Performance Metrics

Monitor these during testing:

- **Badge disappearance latency:** Should be 0ms (optimistic)
- **Backend confirmation time:** Should be < 5 seconds (typically 1-2s)
- **Refresh count:** Exactly 7 refreshes over 20 seconds
- **Memory usage:** No leaks or spikes
- **Battery impact:** Minimal (coroutines are efficient)

---

## 📝 Test Report Template

```
Date: [DATE]
Tester: [NAME]
Device: [DEVICE/EMULATOR]
Android Version: [VERSION]
App Version: [VERSION]

Test Results:
- Test 1: [PASS/FAIL] - Notes: _________
- Test 2: [PASS/FAIL] - Notes: _________
- Test 3: [PASS/FAIL] - Notes: _________
- Test 4: [PASS/FAIL] - Notes: _________
- Test 5: [PASS/FAIL] - Notes: _________
- Test 6: [PASS/FAIL] - Notes: _________
- Test 7: [PASS/FAIL] - Notes: _________
- Test 8: [PASS/FAIL] - Notes: _________
- Test 9: [PASS/FAIL] - Notes: _________
- Test 10: [PASS/FAIL] - Notes: _________

Overall: [PASS/FAIL]

Issues Found:
1. _________
2. _________

Recommendations:
1. _________
2. _________
```

---

**Happy Testing! 🎉**

If you find any issues, check:
1. Logcat logs (filter: `GroupChatItem|MessagesListScreen|ChatStateManager`)
2. Backend logs for WebSocket events
3. Network connectivity
4. Token validity

For support, refer to: `BADGE_PERSISTENCE_COMPLETE_FIX.md`

