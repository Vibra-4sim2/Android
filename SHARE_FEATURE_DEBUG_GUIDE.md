# 🔍 SHARE FEATURE - DEBUG GUIDE

## ❌ Current Problem

**What you see:** Old message format with emoji "🚴 Sortie: test msg" (truncated)  
**What you should see:** Beautiful card with sortie image, title, creator

## 🧪 Debug Steps

### Step 1: Test if NEW share dialog opens
1. Open sortie details
2. Click the share icon (top right)
3. **Check:** Does a dialog open with "Partager dans une discussion" title?
   - ✅ YES → Go to Step 2
   - ❌ NO → Old code is still running

### Step 2: Check if discussions list appears
1. In the share dialog
2. **Check:** Do you see a list of your discussions?
   - ✅ YES → Go to Step 3
   - ❌ NO → MessagesViewModel not loading

### Step 3: Share and check logs
1. Select a discussion from the list
2. **Check Logcat** for tag `ShareSortie`:
   ```
   ShareSortie: ========================================
   ShareSortie: 📤 Sharing sortie to chat: [name]
   ShareSortie: Message to send:
   ShareSortie: SHARED_SORTIE:...
   ShareSortie: TITLE:...
   ShareSortie: CREATOR:...
   ShareSortie: IMAGE:...
   ShareSortie: DATE:...
   ShareSortie: TYPE:...
   ShareSortie: ========================================
   ```
   - ✅ Logs appear → Message sent correctly
   - ❌ No logs → Dialog not using new code

### Step 4: Check message in chat
1. Go to Discussions tab
2. Open the discussion you shared to
3. Scroll to the bottom
4. **Check:** Last message should be:
   - ✅ Beautiful card with image/icon, "Sortie partagée" badge
   - ❌ Plain text → Card rendering not working

---

## 🔧 Possible Issues & Fixes

### Issue 1: Old share code is still running
**Symptom:** No dialog opens, message sent immediately  
**Fix:** Need to remove old share implementation

### Issue 2: Message format is wrong
**Symptom:** Logs show different format than expected  
**Check:** Look for message starting with `SHARED_SORTIE:`

### Issue 3: Card not rendering
**Symptom:** Message appears but as plain text  
**Check:** In ChatConversationScreen, message content should trigger `isSharedSortie = true`

---

## 📝 Expected Message Format

The message sent should look EXACTLY like this:
```
SHARED_SORTIE:692084c38c134f45d4c7078c
TITLE:test msg
CREATOR:Mohamed Amine
IMAGE:https://res.cloudinary.com/...
DATE:2024-06-15T09:00:00.000Z
TYPE:RANDONNEE
```

**NOT** like this:
```
🚴 Sortie: test msg 

Rejoins-moi pour cette aventure!
ID: ...
```

---

## 🎯 Quick Test

Run this test NOW:

1. **Open sortie details** (any sortie)
2. **Click share icon**
3. **Take screenshot** of what you see
4. **Share here** so I can see if the dialog opened

If the dialog doesn't open → Old code is still active, need to find and remove it.

---

## 🔍 What to Look For in Logs

When you share, filter Logcat by:
- Tag: `ShareSortie`
- Tag: `SocketService` (to see message being sent)
- Tag: `ChatConversation` (to see message being received)

Look for the EXACT message content being sent via Socket.IO.


