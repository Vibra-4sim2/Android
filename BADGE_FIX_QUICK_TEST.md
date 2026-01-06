# 🧪 Badge Fix Testing Guide - Quick Start

**Date:** December 27, 2025  
**Purpose:** Quick guide to test the badge persistence fix

---

## 🎯 What Was Fixed

### Problem 1: Badge doesn't disappear
**Before:** Badge stays visible even after viewing messages  
**After:** ✅ Badge disappears within 500ms-1.5s

### Problem 2: Badge never reappears for new messages
**Before:** Once badge disappears, it NEVER shows up again, even with new messages  
**After:** ✅ Badge correctly reappears when new messages arrive

---

## 🚀 Quick Test (5 minutes)

### Setup
1. Open the app with **2 users** (User A and User B)
2. Have a discussion that both users can access

### Test Steps

#### ✅ Test 1: Badge Disappears
```
1. User A: Send a message in the discussion
2. User B: Login and go to Messages screen
   → Expected: Red badge "1" appears ✅
3. User B: Tap on the discussion
4. User B: View the message
5. User B: Press back button
   → Expected: Badge disappears within 1-2 seconds ✅
```

#### ✅ Test 2: Badge Reappears (CRITICAL!)
```
6. User A: Send another NEW message
7. Wait 2-3 seconds
8. User B: Check Messages screen
   → Expected: Badge REAPPEARS showing "1" ✅
```

**If Test 2 FAILS (badge doesn't reappear):**
- The old bug is still present
- Check logcat for "🆕 NEW MESSAGE detected!"

---

## 📋 Detailed Test Cases

### Test A: Single Message Flow
```
User A sends message → User B sees badge → 
User B opens chat → Badge disappears → 
User B closes chat → Badge stays gone ✅
```

### Test B: New Message After Viewing
```
User A sends message → User B views it (badge gone) →
User A sends NEW message → Badge REAPPEARS ✅
```

### Test C: Multiple Discussions
```
3 chats with unread messages →
Open Chat 1 → Badge 1 gone, 2&3 remain →
Open Chat 2 → Badge 2 gone, only 3 remains →
New message to Chat 1 → Badge 1 REAPPEARS ✅
```

### Test D: Fast Navigation
```
Open chat → Immediately press back →
Badge disappears optimistically →
Wait 2 seconds → Badge stays gone ✅
```

---

## 🔍 What to Check in Logcat

### When badge should disappear:
```
MessagesListScreen: 🔄 Refresh #1: After 500ms (backend sync time)
GroupChatItem: ✅ Backend confirmed read (unreadCount=0)
```

### When new message arrives:
```
GroupChatItem: 🆕 NEW MESSAGE detected! Clearing optimistic state
GroupChatItem:    Old timestamp: 2025-12-27T10:30:00
GroupChatItem:    New timestamp: 2025-12-27T10:35:00
```

### When badge should be visible:
```
GroupChatItem: 🔴 Badge should be VISIBLE - unread message exists
GroupChatItem:    unreadCount: 1
GroupChatItem:    effectiveUnreadCount: 1
```

---

## ✅ Success Criteria

- [ ] Badge disappears within 2 seconds after viewing messages
- [ ] Badge REAPPEARS when new messages arrive (CRITICAL!)
- [ ] Badge works independently for multiple discussions
- [ ] Badge persists across app restarts (shows unread on reopen)
- [ ] Fast navigation doesn't break badge behavior

---

## 🐛 If Badge Still Doesn't Work

### Symptom: Badge doesn't disappear
**Check:**
1. Is the first refresh delayed by 500ms? Check logcat for "After 500ms"
2. Is backend returning `unreadCount=0`? Check API response
3. Are WebSocket `markAsRead` events being sent?

### Symptom: Badge never reappears for new messages
**Check:**
1. Is `group.timestamp` changing? Check logcat
2. Is the LaunchedEffect detecting timestamp change?
3. Look for "🆕 NEW MESSAGE detected!" in logcat
4. If missing, the fix didn't apply correctly

### Symptom: Badge behavior is random
**Check:**
1. Are there multiple ON_RESUME events firing?
2. Is optimistic state being cleared/set correctly?
3. Check ChatStateManager logs for state changes

---

## 🎉 Expected Result

After the fix:
1. **Badge disappears** when you view messages ✅
2. **Badge reappears** when new messages arrive ✅
3. **No more stuck badges** that never disappear ✅
4. **No more invisible badges** that never reappear ✅

---

## 📞 Troubleshooting

### Problem: Badge reappears after disappearing
- This is normal if backend is slow (< 2 seconds is OK)
- The refresh cycle handles this automatically
- Badge should be gone by the 3rd or 4th refresh

### Problem: Badge stays visible forever
- Backend might not be processing markAsRead events
- Check WebSocket connection
- Check backend logs for read receipts

### Problem: App crashes when opening chat
- Check ChatConversationScreen for errors
- Verify sortieId is passed correctly
- Check ChatStateManager initialization

---

**Quick Test Complete! 🎉**

If all 5 checkboxes in "Success Criteria" are checked, the fix is working correctly!

