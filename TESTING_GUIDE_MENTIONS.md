# Quick Testing Guide - Mention & Feedback Features

## 🎯 What to Test

### Test 1: Mention Selection
1. Open the app and navigate to "Add Publication" screen
2. Type some content (e.g., "Hello everyone!")
3. Click the **"Mention"** button (blue button with @ icon)
4. **Expected:** Dialog opens showing your followers list
5. Select 1-3 followers by clicking checkboxes
6. **Expected:** Checkboxes get checked
7. Click "Mention (X)" button at the bottom
8. **Expected:** Dialog closes and blue chips appear below content showing "@FirstName LastName"

### Test 2: Remove Mentions
1. After adding mentions (see Test 1)
2. Click the X icon on any mention chip
3. **Expected:** That mention is removed from the list

### Test 3: Publish with Success Feedback
1. Complete Test 1 (add mentions)
2. Add some content and optionally add tags/image
3. Click **"Publish on Feed"** button
4. **Expected Results:**
   - Button shows "Publishing..." with loading spinner
   - After 1-2 seconds, green Snackbar appears at bottom:
     - ✅ "Publication created successfully! 🎉"
   - Snackbar is visible for ~1.5 seconds
   - Screen automatically navigates to Feed
   - New publication appears at the top of feed

### Test 4: View Mentions in Feed
1. After publishing (Test 3)
2. Look at the newly created post in Feed
3. **Expected:** You should see:
   - Your avatar and name
   - The content you wrote
   - Green chips with #tags (if you added tags)
   - **Blue chips with @mentions** showing the followers you mentioned
   - Location (if you added one)
   - Like/Comment/Share buttons

### Test 5: Multiple Mentions Display
1. Create a publication and mention 4+ followers
2. Publish and view in feed
3. **Expected:** 
   - First 3 mentions shown as blue chips
   - A "+X more" chip appears showing count of remaining mentions

### Test 6: Error Handling
1. Turn off WiFi/Data to simulate network error
2. Try to publish a post
3. **Expected:**
   - Red Snackbar appears with error message
   - ❌ Error icon shown
   - You stay on Add Publication screen
   - Can click "OK" to dismiss error

### Test 7: Empty Content Validation
1. Open Add Publication screen
2. Don't type anything (leave content empty)
3. Try to click "Publish on Feed"
4. **Expected:** Button is disabled (grayed out), nothing happens

## 🔍 What to Look For

### Visual Indicators:
- ✅ **Green** = Success (GreenAccent #4ADE80)
- ❌ **Red** = Error (RedAccent #FF3B30)
- 🏷️ **Green chips** = Tags (#hashtag)
- 👤 **Blue chips** = Mentions (@username)

### Mention Chip Format:
```
┌─────────────────────┐
│ @ John Doe     ✕    │  ← Blue background
└─────────────────────┘
  ↑           ↑
  @ icon    Close button
```

### Success Snackbar:
```
┌────────────────────────────────────┐
│ ✓ Publication created successfully! 🎉  [OK] │  ← Green
└────────────────────────────────────┘
```

### Error Snackbar:
```
┌────────────────────────────────────┐
│ ✕ Error message here...        [OK] │  ← Red
└────────────────────────────────────┘
```

## 📱 Test Scenarios

### Happy Path:
1. Login → Home → Add Publication
2. Type content: "Great cycling day! 🚴"
3. Click Mention → Select 2 friends → Mention
4. Click Tags → Select "Cycling", "Fitness" → Done
5. Click Publish
6. ✅ See success toast
7. ✅ Navigate to feed
8. ✅ See new post with mentions and tags

### Edge Cases:

#### No Followers:
- Click Mention button
- **Expected:** Dialog shows "No followers yet"

#### Network Error:
- Turn off internet
- Try to publish
- **Expected:** Red error Snackbar with network error message

#### Long Names:
- Mention someone with a very long name
- **Expected:** Chip adjusts width, text doesn't overflow

#### Many Mentions (5+):
- Mention 5 followers
- **Expected:** Shows first 3, then "+2 more"

## 🐛 Known Non-Issues

These are NOT bugs, they're expected behavior:
- Success toast auto-dismisses after 1.5s (by design)
- You can't mention someone who isn't following you (followers only)
- Mentions are stored as user IDs in database, displayed as names
- Avatar in header pulls from current user's database profile

## ✅ Success Criteria

All these should work:
- [x] Can select followers to mention
- [x] Selected followers appear as chips
- [x] Can remove mentions before publishing
- [x] Publish shows loading state
- [x] Success toast appears on success
- [x] Error toast appears on failure
- [x] Auto-navigate to feed on success
- [x] Mentions visible in feed as blue chips
- [x] Mentions show correct user names
- [x] "+X more" works for 4+ mentions
- [x] Everything works without breaking existing features

## 🔧 Troubleshooting

### Issue: No followers in mention dialog
**Solution:** Make sure you have followers in the database. Use another account to follow your test account.

### Issue: Mentions not showing in feed
**Solution:** 
1. Check backend logs to verify mentions were saved
2. Verify API response includes populated mention objects
3. Pull to refresh the feed

### Issue: Success toast not showing
**Solution:** Check logcat for "SUCCESS! Publication ID:" message. If you see it, the backend worked. The toast timing might be too fast.

### Issue: App crashes when clicking Publish
**Solution:** Check logcat for error messages. Likely causes:
- Network issue
- Invalid user token
- Missing user ID in preferences

## 📊 Test Results Template

```
Date: _______________
Tester: _______________

✅ Test 1: Mention Selection        [ PASS / FAIL ]
✅ Test 2: Remove Mentions           [ PASS / FAIL ]
✅ Test 3: Publish Success Feedback  [ PASS / FAIL ]
✅ Test 4: View Mentions in Feed     [ PASS / FAIL ]
✅ Test 5: Multiple Mentions         [ PASS / FAIL ]
✅ Test 6: Error Handling            [ PASS / FAIL ]
✅ Test 7: Empty Content Validation  [ PASS / FAIL ]

Notes:
_________________________________
_________________________________
_________________________________
```

---

**Ready to test!** 🚀

