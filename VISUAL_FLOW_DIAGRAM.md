# 🎨 Visual Flow Diagram - Mention & Avatar Features

## 📱 Screen Layout

```
╔══════════════════════════════════════════════════════════╗
║  ← Back                          Post ✓                  ║
╠══════════════════════════════════════════════════════════╣
║                                                          ║
║  ┌────────┐                                              ║
║  │  👤    │  John Doe                                    ║
║  │ Avatar │  ← LOADED FROM DATABASE                      ║
║  └────────┘  (or initials if no avatar)                  ║
║                                                          ║
║  ┌────────────────────────────────────────────────────┐  ║
║  │ What's on your mind?                               │  ║
║  │                                                    │  ║
║  │ [User types content here...]                       │  ║
║  └────────────────────────────────────────────────────┘  ║
║                                                          ║
║  ┌─── Selected Tags ───┐                                 ║
║  │ #Cycling  #Fitness │                                  ║
║  └────────────────────┘                                  ║
║                                                          ║
║  ┌─── Mentioned Users ───┐                               ║
║  │ @Alice Smith  @Bob Jones │  ← NEW FEATURE             ║
║  └───────────────────────────┘                           ║
║                                                          ║
║  [Image Preview if selected]                             ║
║                                                          ║
║  ┌──────────┐  ┌──────────┐  ┌──────────┐              ║
║  │ 📷 Photo │  │ 🏷️ Tags  │  │ @ Mention│  ← CLICKS HERE║
║  └──────────┘  └──────────┘  └──────────┘              ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
```

---

## 🔄 Mention Feature Flow

### Step 1: User Clicks @ Mention Button
```
┌─────────────┐
│   User      │
│  clicks     │──────┐
│ @ button    │      │
└─────────────┘      │
                     ▼
              ┌──────────────┐
              │ Show Dialog  │
              │  + Loading   │
              └──────────────┘
```

### Step 2: Fetch Followers from API
```
┌──────────────────────────────────────────┐
│  MentionSelectionDialog Component        │
├──────────────────────────────────────────┤
│                                          │
│  1. Get userId from UserPreferences      │
│  2. Get token from UserPreferences       │
│  3. Call API:                            │
│     GET /user/{userId}/followers         │
│     Header: Bearer {token}               │
│                                          │
│  ┌────────┐                              │
│  │ API ⚡ │───▶ Success ✓                │
│  └────────┘         │                    │
│                     ▼                    │
│         ┌──────────────────┐             │
│         │ Show Follower    │             │
│         │     List         │             │
│         └──────────────────┘             │
│                                          │
└──────────────────────────────────────────┘
```

### Step 3: Display Followers List
```
╔══════════════════════════════════════════╗
║  @ Mention Followers                     ║
╠══════════════════════════════════════════╣
║  ┌────────────────────────────────┐      ║
║  │ ┌──┐  Alice Smith         ☑️   │      ║
║  │ │👤│  alice@email.com           │      ║
║  │ └──┘                            │      ║
║  ├────────────────────────────────┤      ║
║  │ ┌──┐  Bob Jones           ☑️   │      ║
║  │ │👤│  bob@email.com             │      ║
║  │ └──┘                            │      ║
║  ├────────────────────────────────┤      ║
║  │ ┌──┐  Carol White         ☐   │      ║
║  │ │👤│  carol@email.com           │      ║
║  │ └──┘                            │      ║
║  └────────────────────────────────┘      ║
║                                          ║
║         [Cancel]  [Mention (2)]          ║
╚══════════════════════════════════════════╝
```

### Step 4: Display Mention Chips
```
After selection, chips appear:

┌──────────────────────────────────────┐
│ @Alice Smith ✕   @Bob Jones ✕       │
│   (blue chip)      (blue chip)       │
└──────────────────────────────────────┘
       │                    │
       │                    │
       ▼                    ▼
   Click ✕ to remove    Click ✕ to remove
```

---

## 🎯 Avatar Loading Flow

```
┌─────────────────────────────────────────┐
│  AddPublicationScreen Loads             │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│ LaunchedEffect(Unit) {                  │
│   1. Get userId from preferences        │
│   2. Get token from preferences         │
│   3. Call API: GET /user/{userId}       │
│ }                                       │
└────────────┬────────────────────────────┘
             │
             ▼
      ┌──────────────┐
      │  API Call    │
      └──────┬───────┘
             │
        ┌────┴─────┐
        │          │
        ▼          ▼
   ┌────────┐  ┌──────────┐
   │Success │  │  Error   │
   └───┬────┘  └────┬─────┘
       │            │
       ▼            ▼
┌──────────┐  ┌─────────────┐
│ Set vars:│  │Show initials│
│ userName │  │  fallback   │
│userAvatar│  └─────────────┘
│initials  │
└────┬─────┘
     │
     ▼
┌─────────────────────────┐
│ Display Avatar:         │
│                         │
│ if (avatar exists) {    │
│   AsyncImage(avatar)    │
│ } else {                │
│   Text(initials)        │
│ }                       │
└─────────────────────────┘
```

---

## 🔗 Data Flow: Publishing with Mentions

```
┌──────────────────────────────────────────────┐
│ User fills form:                             │
│ • Content: "Great ride today!"               │
│ • Tags: ["Cycling", "Fitness"]               │
│ • Mentions: ["userId1", "userId2"]           │
│ • Image: URI (optional)                      │
└────────────────┬─────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────┐
│ User clicks "Post" button                    │
└────────────────┬─────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────┐
│ viewModel.publishPublication()               │
│                                              │
│ Prepares multipart request:                  │
│ • author = userId                            │
│ • content = "Great ride today!"              │
│ • tags = "Cycling,Fitness"                   │
│ • mentions = "userId1,userId2"  ← COMMA CSV  │
│ • file = image (if selected)                 │
└────────────────┬─────────────────────────────┘
                 │
                 ▼
┌──────────────────────────────────────────────┐
│ PublicationRepository.createPublication()     │
│                                              │
│ Sends to: POST /publication                  │
└────────────────┬─────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
        ▼                 ▼
   ┌─────────┐      ┌──────────┐
   │ SUCCESS │      │  ERROR   │
   └────┬────┘      └────┬─────┘
        │                │
        ▼                ▼
┌──────────────┐  ┌──────────────┐
│Navigate feed │  │Show error msg│
└──────────────┘  └──────────────┘
```

---

## 🗂️ Component Hierarchy

```
AddPublicationScreen
│
├─ TopBar
│  ├─ Back button
│  └─ Post button
│
├─ Avatar Section
│  ├─ AsyncImage (if avatar exists)
│  └─ Initials Text (fallback)
│
├─ Content TextField
│
├─ Tags Section (if tags selected)
│  └─ LazyRow of TagChips
│
├─ Mentions Section (if mentions selected)  ← NEW
│  └─ LazyRow of MentionChips               ← NEW
│
├─ Action Buttons
│  ├─ Photo button
│  ├─ Tags button
│  └─ Mention button                        ← NEW
│
├─ MentionSelectionDialog (conditional)     ← NEW
│  ├─ Loading State
│  ├─ Error State
│  ├─ Empty State
│  └─ Followers List
│     └─ LazyColumn of FollowerItems
│
└─ TagSelectionDialog (conditional)
   └─ Tag chips grid
```

---

## 💾 State Management

```
┌─────────────────────────────────────────┐
│  AddPublicationViewModel                │
├─────────────────────────────────────────┤
│                                         │
│  States:                                │
│  • content: StateFlow<String>           │
│  • selectedTags: StateFlow<List<String>>│
│  • mentionedUsers: StateFlow<List<...>> │  ← NEW
│  • location: StateFlow<String>          │
│  • imageUri: StateFlow<Uri?>            │
│  • uiState: StateFlow<UiState>          │
│                                         │
│  Functions:                             │
│  • updateContent(String)                │
│  • addTag(String)                       │
│  • removeTag(String)                    │
│  • addMention(String)                   │  ← NEW
│  • removeMention(String)                │  ← NEW
│  • setMentions(List<String>)            │  ← NEW
│  • publishPublication()                 │
│                                         │
└─────────────────────────────────────────┘
```

---

## 🎨 Color Coding

```
┌──────────────────────────────────────┐
│  Visual Distinction                  │
├──────────────────────────────────────┤
│                                      │
│  🟢 Green  = Primary actions & avatar│
│             (Post button, avatar     │
│              border, mention button) │
│                                      │
│  🟠 Orange = Tags                    │
│             (#Cycling, #Fitness)     │
│                                      │
│  🔵 Blue   = Mentions                │
│             (@Alice, @Bob)           │
│                                      │
│  ⚪ White  = Primary text            │
│                                      │
│  ⚫ Gray   = Secondary text/borders  │
│                                      │
└──────────────────────────────────────┘
```

---

## 🔄 User Interaction Flow

```
1. Open Screen
   │
   ├─▶ Avatar loads from DB
   │
   └─▶ User sees form

2. Type content
   │
   └─▶ Content state updates

3. Click @ Mention
   │
   ├─▶ Dialog opens
   │
   ├─▶ API fetches followers
   │
   ├─▶ Followers display
   │
   └─▶ User selects followers
       │
       └─▶ Clicks "Mention (X)"
           │
           ├─▶ Dialog closes
           │
           └─▶ Blue chips appear

4. Click Post
   │
   ├─▶ Validation
   │
   ├─▶ API request
   │
   ├─▶ Success
   │
   └─▶ Navigate to feed
```

---

## 📊 API Endpoints Used

```
╔══════════════════════════════════════════════╗
║  Endpoint                    Purpose         ║
╠══════════════════════════════════════════════╣
║  GET /user/{id}             Get user info    ║
║  (for avatar & name)                         ║
║                                              ║
║  GET /user/{id}/followers   Get followers    ║
║  (for mention list)                          ║
║                                              ║
║  POST /publication          Create post      ║
║  (with mentions)                             ║
╚══════════════════════════════════════════════╝
```

---

## ✅ Feature Checklist

```
✓ Avatar Display
  ├─ ✓ Loads from database URL
  ├─ ✓ Shows initials if no avatar
  ├─ ✓ Circular shape
  ├─ ✓ Green border
  └─ ✓ Smooth loading with Coil

✓ Mention Feature
  ├─ ✓ @ button functional
  ├─ ✓ Dialog opens
  ├─ ✓ Fetches followers from API
  ├─ ✓ Shows loading state
  ├─ ✓ Displays follower list
  ├─ ✓ Avatar for each follower
  ├─ ✓ Multi-selection
  ├─ ✓ Mention count updates
  ├─ ✓ Blue chips display
  ├─ ✓ Can remove mentions
  ├─ ✓ Sends to backend
  └─ ✓ Error handling

✓ Integration
  ├─ ✓ Works with existing features
  ├─ ✓ No breaking changes
  ├─ ✓ Proper state management
  └─ ✓ Clean code structure
```

---

**Implementation Complete!** 🎉

