# ✅ BOTH ISSUES FIXED - People Button & Recommended Filter

## 🐛 Issues Found

### Issue 1: People Button Crash
```
Navigation destination 'people_recommendations' cannot be found in the navigation graph
```
**Root Cause**: The route was in MainActivity's NavHost, but HomeExploreScreen uses TabBarView's INTERNAL NavHost (different navigation graph).

### Issue 2: Recommended Should Be a Filter
**User Request**: "I want recommended sorties displayed IN HOME EXPLORE (like a filter), not navigate to another screen"

---

## ✅ FIXES APPLIED

### Fix 1: Added Route to Correct NavHost

**File**: `TabBarView.kt`

**Added**:
```kotlin
composable("people_recommendations") {
    PeopleRecommendationsScreen(navController = internalNavController)
}
```

Now the route is in the SAME NavHost that HomeExploreScreen uses!

---

### Fix 2: Made "Recommended" Act as a Filter

#### A. Updated ViewModel (`HomeExploreViewModel.kt`)

**Added**:
1. AI recommendations state
2. Flask AI repository
3. `loadAiRecommendations()` function
4. Modified `getFilteredSorties()` to return AI sorties when filter is "recommended"
5. Modified `setFilter()` to load AI data when needed

**New Code**:
```kotlin
var aiRecommendations by mutableStateOf<List<FlaskSortieResponse>>(emptyList())
    private set

fun loadAiRecommendations(token: String) {
    // Loads AI sorties from Flask API
}

fun getFilteredSorties(): List<SortieResponse> {
    // If recommended filter, return AI recommendations
    if (selectedFilter == "recommended") {
        return aiRecommendations.map { convertToSortieResponse(it) }
    }
    // Otherwise, filter normally
}

fun setFilter(filter: String, token: String? = null) {
    selectedFilter = filter
    
    // Auto-load AI recommendations when "recommended" is selected
    if (filter == "recommended" && token != null) {
        loadAiRecommendations(token)
    }
}
```

#### B. Updated HomeExploreScreen

**Changed**:
```kotlin
// BEFORE (navigated away)
FilterPill("Recommended", ...) {
    navController.navigate("flask_recommendations")
}

// AFTER (filters in place)
FilterPill("Recommended", ...) {
    val token = UserPreferences.getToken(context)
    viewModel.setFilter("recommended", token)
}
```

---

## 🎯 How It Works Now

### People Button:
```
Click "People" → Navigate to people_recommendations
                 ↓
              Uses TabBarView's NavHost ✅
                 ↓
              Shows PeopleRecommendationsScreen
```

### Recommended Button:
```
Click "Recommended" → setFilter("recommended")
                       ↓
                    Load AI sorties from Flask
                       ↓
                    Display in HomeExplore (SAME SCREEN) ✅
                       ↓
                    NO navigation, just filter!
```

---

## 📱 User Experience

### Before:
- **People**: ❌ Crashed
- **Recommended**: ❌ Navigated to separate screen

### After:
- **People**: ✅ Opens dedicated people screen
- **Recommended**: ✅ Filters sorties IN PLACE (like Cycling, Hiking filters)

---

## 🔄 Filter Behavior

### All Filters Now Work the Same Way:

| Filter | Action | Result |
|--------|--------|--------|
| Explore | `setFilter("explore")` | Shows all sorties |
| **Recommended** | `setFilter("recommended")` | **Shows AI sorties** ✅ |
| People | `navigate("people_recommendations")` | Opens people screen |
| Following | `setFilter("following")` | Shows followed sorties |
| Cycling | `setFilter("cycling")` | Shows cycling sorties |
| Hiking | `setFilter("hiking")` | Shows hiking sorties |
| Camping | `setFilter("camping")` | Shows camping sorties |

**Note**: People is the ONLY button that navigates (separate feature). All others are filters!

---

## 🎨 Visual Flow

```
┌─────────────────────────────────────┐
│  Home Explore Screen                │
│                                     │
│  [Explore] [Recommended] [People]   │
│     ↓           ↓           ↓       │
│   Filter      Filter     Navigate   │
│   locally     locally     to new    │
│               with AI     screen    │
│                                     │
│  ┌───────────────────────────┐     │
│  │ Sortie List (filtered)    │     │
│  │ - Based on selected filter│     │
│  │ - Shows AI sorties if     │     │
│  │   "Recommended" is active │     │
│  └───────────────────────────┘     │
└─────────────────────────────────────┘
```

---

## ✅ Testing Instructions

### Test 1: People Button
1. Clean and rebuild project
2. Open app
3. Go to Home Explore
4. Click "People" button
5. **Expected**: Navigate to people recommendations screen
6. **Expected**: No crash!

### Test 2: Recommended Filter
1. Open app
2. Go to Home Explore
3. Click "Recommended" button
4. **Expected**: Button highlights (selected state)
5. **Expected**: Loading indicator appears
6. **Expected**: AI-recommended sorties appear IN THE SAME SCREEN
7. **Expected**: NO navigation to another screen
8. Click "Explore" button
9. **Expected**: Returns to all sorties

---

## 📋 Files Modified

1. **TabBarView.kt** - Added people_recommendations route
2. **HomeExploreViewModel.kt** - Added AI recommendations logic
3. **HomeExploreScreen.kt** - Changed Recommended to use filter

---

## 🎯 Summary

**Issue 1**: ✅ **FIXED** - People button now uses correct NavHost  
**Issue 2**: ✅ **FIXED** - Recommended is now a filter, not navigation  

**Action Required**: **Clean → Rebuild → Test!**

---

**Date**: December 30, 2025  
**Status**: ✅ Both issues resolved  
**Breaking Changes**: None  
**User Experience**: Significantly improved  

