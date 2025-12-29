# 🧪 COMPLETE TESTING GUIDE - Session Management & Message Badges

## 🎯 TESTING OBJECTIVES

This guide helps you verify:
1. ✅ **Session isolation** - Different users don't see each other's data
2. ✅ **Onboarding flow** - Shown only when needed
3. ✅ **Message badges** - Appear/disappear correctly
4. ✅ **Authentication** - All login methods work properly

---

## 📋 PRE-TESTING CHECKLIST

Before you start testing:

- [ ] Pull latest code from repository
- [ ] Sync Gradle files (File > Sync Project with Gradle Files)
- [ ] Rebuild project (Build > Rebuild Project)
- [ ] Install fresh build on device: `.\gradlew installDebug`
- [ ] Have 2-3 test accounts ready (or create during testing)
- [ ] Have ADB connected for log monitoring

---

## 🧪 TEST SUITE 1: SESSION ISOLATION (CRITICAL)

### Test 1.1: First User Sign Up
**Goal:** Verify new user flow works correctly

**Steps:**
1. Uninstall app (fresh start): `adb uninstall com.example.dam`
2. Install app: `.\gradlew installDebug`
3. Launch app
4. Complete app onboarding (3 screens)
5. Click "Sign Up"
6. Fill form:
   - firstName: "Alice"
   - lastName: "Test"
   - Email: alice@test.com
   - Password: password123
7. Complete sign up

**Expected Result:**
```
✅ Navigate to PreferencesOnboardingScreen
✅ Fill out cycling/hiking/camping preferences
✅ Click "Complete" → Navigate to Home
✅ Profile shows: "Alice Test"
```

**Logs to Check:**
```powershell
adb logcat | Select-String "RegisterScreen|LoginScreen|SplashScreen"
```

Expected:
```
RegisterScreen: ✅ Register réussi
LoginScreen: ✅ Login successful
UserPreferences: ✅ Saved token
UserPreferences: ✅ Saved userId
SplashScreen: → Navigate to PREFERENCES
```

---

### Test 1.2: Alice Logout
**Goal:** Verify logout clears all data

**Steps:**
1. From Alice's session, go to Profile
2. Click logout button
3. Confirm logout

**Expected Result:**
```
✅ Navigate to Login screen
✅ All session data cleared
```

**Logs to Check:**
```powershell
adb logcat | Select-String "LoginViewModel|UserPreferences"
```

Expected:
```
LoginViewModel: 🔴 Starting logout process
LoginViewModel: ✅ Chat disconnected
UserPreferences: 🚪 Clearing user session...
UserPreferences: ✅ Session cleared completely - ready for new user
LoginViewModel: ✅ Logout complete - ready for new user
```

---

### Test 1.3: Second User Sign Up (Bob)
**Goal:** Verify Bob gets fresh session (no Alice data)

**Steps:**
1. From login screen, click "Sign Up"
2. Fill form:
   - firstName: "Bob"
   - lastName: "Smith"
   - Email: bob@test.com
   - Password: password123
3. Complete sign up
4. Complete preferences
5. Navigate to Profile

**Expected Result:**
```
✅ Navigate to Preferences (Bob hasn't completed them yet)
✅ Complete preferences
✅ Navigate to Home
✅ Profile shows: "Bob Smith" (NOT Alice Test!)
✅ No data from Alice visible anywhere
```

**🔴 CRITICAL CHECK:**
- Profile name: "Bob Smith" ✅
- Profile email: bob@test.com ✅
- NO "Alice" anywhere ✅
- NO data leakage ✅

**Logs:**
```powershell
adb logcat | Select-String "UserPreferences|ProfileScreen"
```

Expected:
```
UserPreferences: ✅ Saved userId: <bob_user_id>
UserPreferences: ✅ Onboarding complete: true
ProfileScreen: Loading profile for userId: <bob_user_id>
```

---

### Test 1.4: Bob Logout → Alice Login
**Goal:** Verify Alice's data is preserved and Bob's is cleared

**Steps:**
1. Logout Bob
2. Login with alice@test.com / password123
3. Check profile

**Expected Result:**
```
✅ Skip Preferences (Alice already completed)
✅ Navigate directly to Home
✅ Profile shows: "Alice Test"
✅ Alice's preferences loaded
✅ No Bob data visible
```

**Logs:**
```powershell
adb logcat | Select-String "SplashScreen|LoginScreen"
```

Expected:
```
LoginScreen: ✅ Login successful
LoginScreen: ✅ User has preferences: Navigate to HOME
SplashScreen: ✅ User has completed preferences → Home
```

---

### Test 1.5: App Restart (Alice Still Logged In)
**Goal:** Verify session persists across app restarts

**Steps:**
1. While logged in as Alice, close app (swipe away from recent apps)
2. Relaunch app
3. Wait on Splash screen

**Expected Result:**
```
✅ SplashScreen validates token
✅ Token is valid
✅ Preferences already complete
✅ Navigate directly to Home (skip login, skip preferences)
✅ Profile still shows Alice
```

**Logs:**
```powershell
adb logcat | Select-String "SplashScreen"
```

Expected:
```
SplashScreen: 🔑 token: <token_preview>
SplashScreen: 👤 userId: <alice_user_id>
SplashScreen: 🔐 Token expired: false
SplashScreen: ✅ User has completed preferences → Home
```

---

## 🧪 TEST SUITE 2: MESSAGE BADGES

### Test 2.1: View Unread Messages
**Goal:** Verify badges appear for unread messages

**Steps:**
1. Have another user send you a message in a group chat
2. Navigate to Messages tab
3. Observe the chat list

**Expected Result:**
```
✅ Chat shows red badge with count (e.g., "3")
✅ Last message text is BOLD
✅ Last message is WHITE (not gray)
```

**Visual Check:**
- Badge color: Red (like WhatsApp) ✅
- Badge position: Right side of chat item ✅
- Badge count: Matches actual unread messages ✅

---

### Test 2.2: Open Chat → Badge Disappears
**Goal:** Verify optimistic UI - badge hides immediately

**Steps:**
1. From message list with unread badge
2. Click on the chat with badge
3. Chat conversation opens
4. Press back to return to message list

**Expected Result:**
```
✅ Badge disappears IMMEDIATELY when clicking chat
✅ Badge stays hidden after returning to list
✅ Last message no longer bold
✅ Last message color changes to gray
```

**Timing:**
- Badge should hide in < 100ms (instant)
- Should NOT wait for backend response
- Should stay hidden even if backend hasn't updated yet

**Logs:**
```powershell
adb logcat | Select-String "ChatStateManager|GroupChatItem"
```

Expected:
```
ChatStateManager: ✅ Chat marked as optimistically read: <sortieId>
GroupChatItem: isOptimisticallyRead: true
GroupChatItem: effectiveUnreadCount (displayed): 0
MessagesListScreen: 🔄 Refresh #1: After 500ms
```

---

### Test 2.3: New Message Arrives → Badge Reappears
**Goal:** Verify badge shows for new messages

**Steps:**
1. Have a chat with no badge (all read)
2. Have another user send a new message to that chat
3. Observe message list (may need to pull to refresh)

**Expected Result:**
```
✅ Badge appears with new count
✅ Last message text updates
✅ Last message is BOLD again
✅ Badge shows correct count
```

**Logs:**
```powershell
adb logcat | Select-String "GroupChatItem|unreadCount"
```

Expected:
```
GroupChatItem: unreadCount (from backend): 1
GroupChatItem: 🆕 NEW MESSAGE detected! Clearing optimistic state
GroupChatItem: effectiveUnreadCount (displayed): 1
```

---

### Test 2.4: Multiple Refreshes
**Goal:** Verify refresh cycles work correctly

**Steps:**
1. Mark messages as read by opening chat
2. Return to message list
3. Watch logs for refresh cycles

**Expected Result:**
```
✅ 5 refresh cycles over 10 seconds
✅ Each refresh calls backend
✅ Badge stays hidden during refreshes
✅ unreadCount eventually becomes 0 from backend
```

**Logs:**
```powershell
adb logcat | Select-String "MessagesListScreen.*Refresh"
```

Expected:
```
MessagesListScreen: 🔄 Refresh #1: After 500ms
MessagesListScreen: 🔄 Refresh #2: After 1.5s
MessagesListScreen: 🔄 Refresh #3: After 3s
MessagesListScreen: 🔄 Refresh #4: After 5s
MessagesListScreen: 🔄 Refresh #5 (FINAL): After 10s
MessagesListScreen: ✅ Refresh cycle complete
```

---

### Test 2.5: Logout → Different User Messages
**Goal:** Verify message badges are user-specific

**Steps:**
1. User A has 3 unread messages (badge shows "3")
2. Logout User A
3. Login as User B
4. Navigate to Messages tab

**Expected Result:**
```
✅ User B sees ONLY their own chats
✅ User B's badge counts are independent
✅ NO badges from User A visible
✅ Different chat groups (if User B is in different groups)
```

**🔴 CRITICAL:**
- User A's messages NOT visible to User B ✅
- Badge counts are per-user ✅
- Complete session isolation ✅

---

## 🧪 TEST SUITE 3: GOOGLE SIGN-IN

### Test 3.1: First-Time Google Sign-In
**Goal:** Verify Google authentication flow

**Steps:**
1. Logout from any current session
2. Click "Sign in with Google"
3. Select Google account (first time)
4. Authorize app

**Expected Result:**
```
✅ Backend creates account
✅ Token returned
✅ Navigate to Preferences (first time)
✅ Complete preferences
✅ Navigate to Home
```

---

### Test 3.2: Returning Google Sign-In
**Goal:** Verify returning Google users skip preferences

**Steps:**
1. Logout
2. Click "Sign in with Google"
3. Select SAME Google account

**Expected Result:**
```
✅ Backend recognizes account
✅ Token returned
✅ Skip Preferences
✅ Navigate directly to Home
```

---

## 🧪 TEST SUITE 4: EDGE CASES

### Test 4.1: Token Expiration
**Goal:** Verify expired token handling

**Steps:**
1. Login and stay logged in
2. Wait for token to expire (or manually corrupt it)
3. Reopen app

**Expected Result:**
```
✅ SplashScreen detects expired token
✅ Shows alert: "Session Expired"
✅ Clears session
✅ Navigate to Login
```

---

### Test 4.2: Network Error During Preferences Check
**Goal:** Verify offline mode fallback

**Steps:**
1. Login successfully
2. Enable airplane mode
3. Close and reopen app

**Expected Result:**
```
✅ SplashScreen tries to check preferences
✅ Network error occurs
✅ Falls back to local cache
✅ Navigate to Home (if local cache says complete)
```

---

### Test 4.3: Rapid User Switching
**Goal:** Stress test session isolation

**Steps:**
1. Login as Alice → Check profile
2. Logout
3. Login as Bob → Check profile
4. Logout
5. Login as Charlie → Check profile
6. Logout
7. Login as Alice again → Check profile

**Expected Result:**
```
✅ Each login shows correct user data
✅ No data leakage between sessions
✅ Profiles update correctly each time
```

---

## 🐛 TROUBLESHOOTING

### Issue: Badge doesn't disappear after opening chat

**Debug Steps:**
1. Check logs:
```powershell
adb logcat | Select-String "ChatStateManager|markAsRead"
```

2. Expected logs:
```
ChatStateManager: ✅ Chat marked as optimistically read
ChatConversation: 📤 Marking messages as read
MessagesViewModel: ✅ Messages marked as read successfully
```

3. If missing:
   - Check ChatConversationScreen calls `markAsRead()`
   - Verify backend `/messages/mark-read` endpoint exists

---

### Issue: Profile shows wrong user data

**Debug Steps:**
1. Check SharedPreferences:
```powershell
adb logcat | Select-String "UserPreferences.*userId"
```

2. Expected:
```
UserPreferences: ✅ Saved userId: <current_user_id>
```

3. Check ProfileScreen:
```powershell
adb logcat | Select-String "ProfileScreen"
```

4. Verify it uses `UserPreferences.getUserId()` not direct SharedPreferences

---

### Issue: Onboarding shown every time

**Debug Steps:**
1. Check preferences status:
```powershell
adb logcat | Select-String "onboardingComplete"
```

2. After completing preferences:
```
UserPreferences: ✅ Onboarding complete: true
```

3. On app restart:
```
SplashScreen: ✅ User has completed preferences → Home
```

---

## 📊 TEST RESULTS CHECKLIST

After completing all tests:

### Session Isolation
- [ ] Different users see different profiles
- [ ] Logout clears all user data
- [ ] No data leakage between sessions
- [ ] Onboarding shown only when needed

### Message Badges
- [ ] Badges appear for unread messages
- [ ] Badges disappear immediately when opening chat
- [ ] Badges reappear for new messages
- [ ] Badges are user-specific

### Authentication
- [ ] Regular login works
- [ ] Regular signup works
- [ ] Google Sign-In works (first time)
- [ ] Google Sign-In works (returning)
- [ ] Token expiration handled
- [ ] App restart preserves session

### Edge Cases
- [ ] Network errors handled gracefully
- [ ] Rapid user switching works
- [ ] Offline mode uses cache
- [ ] Token validation works

---

## 🎉 SUCCESS CRITERIA

All tests pass if:
✅ No user data leakage between accounts
✅ Badges appear/disappear correctly
✅ All authentication methods work
✅ Session persists across app restarts
✅ Logout completely clears session

---

## 📝 REPORTING ISSUES

If a test fails, provide:
1. Test number (e.g., "Test 1.3 failed")
2. Expected vs actual result
3. Logs from the test
4. Screenshots if UI issue
5. Device info (Android version, model)

---

**Last Updated:** December 28, 2025  
**Status:** ✅ READY FOR TESTING  
**Estimated Time:** 30-45 minutes for complete test suite

