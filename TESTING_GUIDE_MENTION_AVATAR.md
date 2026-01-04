# 🧪 Testing Guide - Mention & Avatar Features

## Prerequisites
- Ensure backend server is running
- Have at least one user account with followers
- Make sure the test user has an avatar set in the database

---

## Test 1: Avatar Display ✅

### Steps:
1. Open the app and login
2. Navigate to "Add Publication" screen (click + button from home/feed)
3. Observe the avatar in the top-left corner

### Expected Results:
- ✅ Avatar should load from database if user has one
- ✅ Avatar should be circular with green border
- ✅ If no avatar, user's initials should display (e.g., "JD" for John Doe)
- ✅ Avatar should load smoothly without flickering

### Troubleshooting:
- If avatar doesn't load: Check network connection and backend URL
- If initials show but avatar exists: Check avatar URL format in database
- Verify avatar URL is publicly accessible

---

## Test 2: Mention Feature - Opening Dialog ✅

### Steps:
1. On "Add Publication" screen
2. Click the "Mention" button (blue @ icon in the action row)

### Expected Results:
- ✅ Dialog opens with title "Mention Followers"
- ✅ Green @ icon appears in dialog title
- ✅ Loading spinner shows while fetching followers

### Troubleshooting:
- If dialog doesn't open: Check console for errors
- If loading never ends: Check backend API endpoint `/user/{userId}/followers`

---

## Test 3: Follower List Display ✅

### After dialog opens and loads:

### Expected Results - When you have followers:
- ✅ List of followers displays in scrollable view
- ✅ Each follower shows:
  - Avatar or initials
  - Full name (firstName + lastName)
  - Email address
  - Checkbox for selection
- ✅ Avatars are circular with green border
- ✅ List is scrollable if more than 5-6 followers

### Expected Results - When you have NO followers:
- ✅ Icon showing "no followers" (person with slash)
- ✅ Text: "No followers yet"
- ✅ Clean empty state UI

### Troubleshooting:
- If error shows: Check API response format
- If empty when followers exist: Check userId in API call
- Verify token is valid and passed correctly

---

## Test 4: Selecting Followers ✅

### Steps:
1. In the mention dialog with followers visible
2. Click checkbox next to 1-3 different followers
3. Observe the "Mention" button at bottom

### Expected Results:
- ✅ Checkbox becomes checked when clicked
- ✅ Follower row highlights with green tint when selected
- ✅ Confirm button updates: "Mention (1)", "Mention (2)", "Mention (3)"
- ✅ Can select multiple followers
- ✅ Can uncheck to deselect

---

## Test 5: Mention Chips Display ✅

### Steps:
1. Select 2-3 followers in dialog
2. Click "Mention (X)" button to confirm
3. Observe the area below the text input field

### Expected Results:
- ✅ Dialog closes
- ✅ Blue chips appear showing "@FirstName LastName"
- ✅ Each chip has:
  - Blue gradient background
  - Blue border
  - @ symbol + user name
  - X icon to remove
- ✅ Chips are arranged horizontally in a scrollable row
- ✅ Multiple chips display side by side

---

## Test 6: Removing Mentions ✅

### Steps:
1. After adding mentions (blue chips visible)
2. Click the X icon on any mention chip

### Expected Results:
- ✅ Clicked chip disappears immediately
- ✅ Other chips remain unchanged
- ✅ If all chips removed, mention row disappears

---

## Test 7: Publishing with Mentions ✅

### Steps:
1. Add some text content
2. Add 1-2 mentions using @ button
3. Optionally add tags or image
4. Click "Post" button in top-right
5. Check the feed after publication

### Expected Results:
- ✅ Publication created successfully
- ✅ Navigates to feed screen
- ✅ New publication appears in feed
- ✅ Backend receives mention IDs in request
- ✅ No errors in console

### Backend Verification:
```json
// Check backend receives:
{
  "author": "userId123",
  "content": "Your text here",
  "mentions": "userId1,userId2,userId3"  // Comma-separated IDs
}
```

---

## Test 8: Combined Features ✅

### Steps:
1. Create a new publication with:
   - Text content
   - Image (click Photo button)
   - Tags (click Tags button, select 2-3)
   - Mentions (click Mention button, select 2-3)
2. Verify all elements show:
   - User avatar at top
   - Text content
   - Image preview
   - Orange tag chips
   - Blue mention chips
3. Click Post

### Expected Results:
- ✅ All elements display correctly
- ✅ Avatar loads properly
- ✅ Tags shown in orange
- ✅ Mentions shown in blue
- ✅ Can scroll if content is long
- ✅ Publication creates successfully
- ✅ All data sent to backend

---

## Test 9: Edge Cases ✅

### Test A: No Followers
1. Use account with 0 followers
2. Click Mention button
3. **Expected:** "No followers yet" message with icon

### Test B: No Avatar
1. Use account without avatar in database
2. Open Add Publication screen
3. **Expected:** Initials display instead (e.g., "AB")

### Test C: Cancel Dialog
1. Open mention dialog
2. Select some followers
3. Click "Cancel"
4. **Expected:** Dialog closes, no mentions added

### Test D: Empty Selection
1. Open mention dialog
2. Don't select anyone
3. **Expected:** "Mention (0)" button is disabled/grayed

### Test E: Network Error
1. Turn off backend server
2. Click Mention button
3. **Expected:** Error message shows in dialog

---

## Test 10: Performance ✅

### Steps:
1. Open mention dialog with 20+ followers
2. Scroll through list
3. Select/deselect multiple users

### Expected Results:
- ✅ Smooth scrolling
- ✅ No lag when selecting
- ✅ Avatars load progressively
- ✅ No crashes or freezing
- ✅ Dialog responsive

---

## 🐛 Common Issues & Solutions

### Avatar Not Loading
**Symptom:** Initials show but avatar exists in DB  
**Fix:** 
- Check avatar URL format (must be full URL)
- Verify Coil dependency in build.gradle
- Check network permissions in AndroidManifest

### Mention Dialog Empty
**Symptom:** Dialog opens but shows nothing  
**Fix:**
- Check console for API errors
- Verify token is valid
- Check followers API endpoint response format
- Ensure FollowersResponse model matches backend

### Chips Not Showing
**Symptom:** Select users but chips don't appear  
**Fix:**
- Check viewModel.setMentions() is called
- Verify mentionedUsers state flow
- Check LazyRow visibility condition

### Publication Not Creating
**Symptom:** Post button clicked but nothing happens  
**Fix:**
- Check content is not blank
- Verify backend receives request
- Check console for errors
- Verify mention IDs format (comma-separated)

---

## ✅ Success Criteria

All tests should pass:
- [x] Avatar displays from database
- [x] Mention button opens dialog
- [x] Followers load from API
- [x] Can select multiple followers
- [x] Chips display correctly
- [x] Can remove mentions
- [x] Publication creates with mentions
- [x] No crashes or errors
- [x] Smooth performance

---

## 📊 Test Results Template

```
Date: ___________
Tester: ___________

Test 1 (Avatar): ☐ Pass ☐ Fail
Test 2 (Dialog): ☐ Pass ☐ Fail
Test 3 (List):   ☐ Pass ☐ Fail
Test 4 (Select): ☐ Pass ☐ Fail
Test 5 (Chips):  ☐ Pass ☐ Fail
Test 6 (Remove): ☐ Pass ☐ Fail
Test 7 (Publish):☐ Pass ☐ Fail
Test 8 (Combined):☐ Pass ☐ Fail
Test 9 (Edge):   ☐ Pass ☐ Fail
Test 10 (Perf):  ☐ Pass ☐ Fail

Issues Found:
_________________________________
_________________________________
_________________________________

Overall Status: ☐ APPROVED ☐ NEEDS FIXES
```

---

**Ready to Test!** 🚀

