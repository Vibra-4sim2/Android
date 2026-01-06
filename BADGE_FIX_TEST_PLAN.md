# 🧪 BADGE FIX - TESTING PLAN

## 📋 Pre-Test Setup

### Requirements
- [ ] 2 test accounts (User A and User B)
- [ ] Android device or emulator
- [ ] Android Studio with Logcat open
- [ ] Clean app installation (clear data/cache)

### Logcat Setup
1. Open Android Studio Logcat
2. Apply filter:
   ```
   tag:ChatViewModel | tag:ChatStateManager | tag:MessagesListScreen | tag:GroupChatItem
   ```
3. Clear existing logs before each test

---

## 🎯 Test Cases

### Test 1: Single Unread Message (Basic Scenario)

#### Setup
1. Login as User A
2. Go to a discussion
3. Send 1 text message
4. Logout
5. Login as User B

#### Test Steps
1. Open Messages screen
2. **VERIFY:** Red badge "1" appears on the discussion ✅
3. Tap the discussion to open chat
4. **VERIFY:** Badge disappears IMMEDIATELY (within 100ms) ✅
5. View/read the message
6. Wait 2 seconds
7. Press back button
8. **VERIFY:** Badge is HIDDEN ✅
9. Wait 5 seconds
10. **VERIFY:** Badge is still HIDDEN ✅
11. Wait 10 seconds
12. **VERIFY:** Badge is still HIDDEN ✅
13. Wait 20 seconds total
14. **VERIFY:** Badge is still HIDDEN ✅

#### Expected Logcat
```
ChatStateManager: ✅ Chat marked as opened (optimistic)
ChatViewModel: 📖 Marquage de 1 messages comme lus
MessagesListScreen: 🔄 ON_RESUME: Immediate refresh...
MessagesListScreen: 🔄 ON_RESUME: Second refresh after 2s...
MessagesListScreen: 🔄 ON_RESUME: Third refresh after 5s...
MessagesListScreen: 🔄 ON_RESUME: Fourth refresh after 10s...
MessagesListScreen: 🔄 ON_RESUME: Final refresh after 15s...
MessagesListScreen: ✅ Refresh cycle complete. Optimistic states kept.
GroupChatItem: ✅ Backend confirmed, clearing optimistic state
```

#### Pass Criteria
- [ ] Badge appears initially
- [ ] Badge disappears instantly when opening chat
- [ ] Badge never reappears for at least 20 seconds
- [ ] All expected logs present

---

### Test 2: Multiple Unread Messages

#### Setup
1. Login as User A
2. Go to a discussion
3. Send 5-10 text messages
4. Logout
5. Login as User B

#### Test Steps
1. Open Messages screen
2. **VERIFY:** Red badge "1" appears ✅
3. Tap the discussion
4. **VERIFY:** Badge disappears IMMEDIATELY ✅
5. Scroll through all messages
6. Wait 3 seconds
7. Press back button
8. **VERIFY:** Badge is HIDDEN ✅
9. Wait 20 seconds
10. **VERIFY:** Badge is still HIDDEN ✅

#### Expected Logcat
```
ChatViewModel: 📖 Marquage de 5 messages comme lus
ChatViewModel:    📧 Message 1/5: ...
(50ms delays between each)
ChatViewModel:    📧 Message 5/5: ...
ChatViewModel: ✅ All 5 messages marked with delays
```

#### Pass Criteria
- [ ] All messages marked with delays
- [ ] Badge never reappears
- [ ] 50ms delays visible in logs

---

### Test 3: Fast Navigation (Edge Case)

#### Setup
1. Have User A send a message
2. Login as User B
3. Open Messages screen with badge "1"

#### Test Steps
1. Tap the discussion
2. **IMMEDIATELY** press back (within 500ms)
3. **VERIFY:** Badge disappears or is hidden ✅
4. Wait 20 seconds
5. **VERIFY:** Badge eventually disappears ✅

#### Expected Behavior
- Badge may briefly appear (user was too fast)
- Badge disappears within 15 seconds due to refresh cycles
- Final markAsRead in leaveRoom() ensures messages marked

#### Pass Criteria
- [ ] Badge disappears within 15 seconds max
- [ ] No crashes or errors
- [ ] leaveRoom() logs show final markAsRead

---

### Test 4: Multiple Discussions

#### Setup
1. Create 3 different discussions
2. Have User A send 1 message in each
3. Login as User B

#### Test Steps
1. Open Messages screen
2. **VERIFY:** All 3 discussions show badge "1" ✅
3. Open Discussion 1
4. **VERIFY:** Badge 1 disappears instantly ✅
5. Press back
6. **VERIFY:** Only Badge 1 is hidden, Badges 2 & 3 still visible ✅
7. Open Discussion 2
8. **VERIFY:** Badge 2 disappears instantly ✅
9. Press back
10. **VERIFY:** Badges 1 & 2 hidden, Badge 3 still visible ✅
11. Open Discussion 3
12. **VERIFY:** Badge 3 disappears instantly ✅
13. Press back
14. **VERIFY:** All badges hidden ✅
15. Wait 20 seconds
16. **VERIFY:** All badges still hidden ✅

#### Expected Logcat
```
ChatStateManager: ✅ Chat marked as opened (optimistic): discussion1
ChatStateManager: ✅ Chat marked as opened (optimistic): discussion2
ChatStateManager: ✅ Chat marked as opened (optimistic): discussion3
...
GroupChatItem: ✅ Backend confirmed, clearing optimistic state for discussion1
GroupChatItem: ✅ Backend confirmed, clearing optimistic state for discussion2
GroupChatItem: ✅ Backend confirmed, clearing optimistic state for discussion3
```

#### Pass Criteria
- [ ] Each badge clears independently
- [ ] No interference between discussions
- [ ] All 3 optimistic states managed correctly

---

### Test 5: Slow Backend Simulation

#### Setup
1. Use Render backend (cold start) or throttle network
2. Have User A send message
3. Login as User B

#### Test Steps
1. Open Messages screen with badge "1"
2. Tap discussion
3. **VERIFY:** Badge disappears instantly ✅
4. Read message
5. Press back
6. **Monitor for 30 seconds**
7. **VERIFY:** Badge stays hidden entire time ✅
8. Check Logcat for backend confirmation time

#### Expected Behavior
- Badge hides instantly (optimistic)
- Backend may take 20+ seconds to confirm
- Badge never reappears during wait

#### Expected Logcat
```
(15 seconds pass)
MessagesListScreen: ✅ Refresh cycle complete. Optimistic states kept.
(5 more seconds)
GroupChatItem: ✅ Backend confirmed, clearing optimistic state
└─ Backend took ~20 seconds total
```

#### Pass Criteria
- [ ] Badge stays hidden for 30+ seconds
- [ ] Eventually backend confirms (check logs)
- [ ] No badge reappearance

---

### Test 6: App Backgrounding

#### Setup
1. Have User A send message
2. Login as User B

#### Test Steps
1. Open Messages screen with badge "1"
2. Tap discussion
3. Badge disappears
4. **Background the app** (press Home button)
5. Wait 5 seconds
6. **Reopen the app**
7. Press back to Messages screen
8. **VERIFY:** Badge is hidden ✅
9. Wait 20 seconds
10. **VERIFY:** Badge stays hidden ✅

#### Pass Criteria
- [ ] App handles backgrounding correctly
- [ ] Optimistic state persists
- [ ] Badge remains hidden after resume

---

### Test 7: Network Interruption

#### Setup
1. Have User A send message
2. Login as User B

#### Test Steps
1. Open Messages screen with badge "1"
2. Tap discussion
3. Badge disappears
4. **Disable WiFi/Data**
5. Wait 2 seconds
6. Press back
7. **VERIFY:** Badge is still hidden (optimistic) ✅
8. **Enable WiFi/Data**
9. Wait 20 seconds
10. **VERIFY:** Badge stays hidden ✅
11. Check Logcat for eventual backend confirmation

#### Pass Criteria
- [ ] Optimistic state works offline
- [ ] Badge stays hidden during network interruption
- [ ] Backend eventually confirms when network restored

---

### Test 8: Rapid Chat Switching

#### Setup
1. Have 2 discussions with unread messages
2. Login as User B

#### Test Steps
1. Open Messages screen
2. Tap Discussion 1 → badge disappears
3. Wait 1 second
4. Press back
5. Tap Discussion 2 → badge disappears
6. Wait 1 second
7. Press back
8. Tap Discussion 1 again
9. Press back
10. **VERIFY:** Both badges hidden ✅
11. Wait 20 seconds
12. **VERIFY:** Both badges still hidden ✅

#### Pass Criteria
- [ ] No state confusion between rapid switches
- [ ] Both optimistic states managed correctly
- [ ] No crashes or errors

---

## 📊 Test Results Template

### Test Date: ___________
### Tester: ___________
### Device: ___________
### Android Version: ___________

| Test # | Test Name | Pass/Fail | Notes |
|--------|-----------|-----------|-------|
| 1 | Single Unread Message | ⬜ | |
| 2 | Multiple Unread Messages | ⬜ | |
| 3 | Fast Navigation | ⬜ | |
| 4 | Multiple Discussions | ⬜ | |
| 5 | Slow Backend | ⬜ | |
| 6 | App Backgrounding | ⬜ | |
| 7 | Network Interruption | ⬜ | |
| 8 | Rapid Chat Switching | ⬜ | |

### Overall Result: ⬜ PASS ⬜ FAIL

### Issues Found:
```
(List any issues here)
```

### Logcat Logs:
```
(Paste relevant logs)
```

---

## 🚨 Failure Analysis

### If Test 1 Fails (Badge Reappears)
1. Check Logcat for premature optimistic state clearing
2. Verify refresh cycle completed (all 5 refreshes)
3. Check backend processing time
4. Increase refresh strategy to 20s if needed

### If Test 2 Fails (Multiple Messages)
1. Verify all messages marked in logs
2. Check for 50ms delays between markings
3. Ensure 200ms final delay present

### If Test 3 Fails (Fast Navigation)
1. Check if leaveRoom() was called
2. Verify final markAsRead in leaveRoom()
3. Check 500ms delay before leaving

### If Test 4 Fails (Multiple Discussions)
1. Verify each discussion has unique optimistic state
2. Check ChatStateManager logs for each sortieId
3. Ensure no state collision

### If Test 5 Fails (Slow Backend)
1. Check actual backend processing time (logs)
2. If >15s consistently, increase refresh to 20s
3. Verify network connectivity

### If Test 6 Fails (Backgrounding)
1. Check if optimistic state persists across lifecycle
2. Verify StateFlow doesn't reset
3. Check ON_RESUME handling

### If Test 7 Fails (Network Interruption)
1. Verify optimistic state works offline
2. Check if WebSocket reconnects properly
3. Ensure backend confirmation after reconnect

### If Test 8 Fails (Rapid Switching)
1. Check for state collisions in logs
2. Verify each chat has independent state
3. Check for race conditions

---

## ✅ Acceptance Criteria

### Must Pass (Critical)
- [ ] Test 1: Single Unread Message
- [ ] Test 2: Multiple Unread Messages
- [ ] Test 4: Multiple Discussions

### Should Pass (Important)
- [ ] Test 3: Fast Navigation
- [ ] Test 5: Slow Backend
- [ ] Test 8: Rapid Chat Switching

### Nice to Pass (Edge Cases)
- [ ] Test 6: App Backgrounding
- [ ] Test 7: Network Interruption

### Overall Success Rate
**Target:** 95%+ of tests pass  
**Minimum:** 85%+ of tests pass

---

## 🔄 Regression Testing

After any code changes, re-run:
1. Test 1 (basic scenario)
2. Test 4 (multiple discussions)
3. Test 5 (slow backend)

---

## 📝 Notes

### Environment Variables
- Backend URL: ___________
- Socket URL: ___________
- Network Speed: ___________

### Known Issues
- (Document any known issues that don't block release)

### Future Improvements
- (Suggestions for v3.0)

---

**Created:** December 27, 2025  
**Version:** v2.0  
**Status:** Ready for Testing

