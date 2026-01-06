package com.example.dam.Screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dam.R
import com.example.dam.models.SortieResponse
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.utils.UserAvatar
import com.example.dam.utils.AvatarCache
import com.example.dam.viewmodel.HomeExploreViewModel
import com.example.dam.viewmodel.DateFilterOption
import com.example.dam.viewmodel.ProximityFilterOption
import com.example.dam.viewmodel.UserProfileViewModel
import com.example.dam.viewmodel.SavedSortiesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeExploreScreen(
    navController: NavController,
    viewModel: HomeExploreViewModel = viewModel(),
    userProfileViewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModelFactory(LocalContext.current)
    ),
    savedSortiesViewModel: SavedSortiesViewModel = viewModel()
) {
    val context = LocalContext.current

    // ✅ Use global theme state
    val themeState = LocalThemeState.current
    val isDarkMode = themeState.isDarkMode

    val token = UserPreferences.getToken(context) ?: ""
    val currentUserId = UserPreferences.getUserId(context) ?: ""

    // ✅ NEW: Bottom Sheet state for advanced filters
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // 🎤 Voice Search Launcher
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenText?.firstOrNull()?.let { text ->
                viewModel.updateSearchQuery(text)
                Toast.makeText(context, "🔍 Searching for: $text", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Charger les utilisateurs suivis
    LaunchedEffect(currentUserId, token) {
        if (currentUserId.isNotEmpty() && token.isNotEmpty()) {
            userProfileViewModel.loadFollowing(currentUserId, token)
        }
    }

    val followingIds by userProfileViewModel.followingIds.collectAsState(initial = emptySet())

    // Charger les sorties sauvegardées LOCALEMENT
    LaunchedEffect(Unit) {
        savedSortiesViewModel.loadSavedSorties(context)
    }

    val savedSortieIds by savedSortiesViewModel.savedSortieIds.collectAsState()

    // Capture filter states to force recalculation when they change
    val currentDateFilter = viewModel.dateFilter
    val currentProximityFilter = viewModel.proximityFilter
    val currentUserLat = viewModel.userLatitude
    val currentUserLng = viewModel.userLongitude

    val filteredSorties = remember(
        viewModel.sorties,
        viewModel.selectedFilter,
        viewModel.searchQuery,
        followingIds,
        currentDateFilter,
        currentProximityFilter,
        currentUserLat,
        currentUserLng
    ) {
        // Debug: Log all sorties dates
        Log.d("FilterDebug", "🔍 ============= FILTER RECALCULATING =============")
        Log.d("FilterDebug", "🔍 Total sorties: ${viewModel.sorties.size}")
        Log.d("FilterDebug", "🔍 Date filter: $currentDateFilter")
        Log.d("FilterDebug", "🔍 Proximity filter: $currentProximityFilter")
        Log.d("FilterDebug", "🔍 User location: $currentUserLat, $currentUserLng")

        var list = viewModel.sorties.filter {
            it.titre.contains(viewModel.searchQuery, ignoreCase = true) ||
                    it.itineraire?.pointArrivee?.displayName?.contains(viewModel.searchQuery, ignoreCase = true) == true
        }

        // Existing filters
        list = when (viewModel.selectedFilter) {
            "following" -> if (followingIds.isNotEmpty()) {
                list.filter { it.createurId.id in followingIds }
            } else list
            "cycling" -> list.filter { it.type == "VELO" }
            "hiking" -> list.filter { it.type == "RANDONNEE" }
            "camping" -> list.filter { it.optionCamping }
            else -> list
        }

        // ✅ NEW: Date filter
        if (currentDateFilter != DateFilterOption.ALL) {
            // Calculate date range based on filter
            val (startDate, endDate) = when (currentDateFilter) {
                DateFilterOption.TODAY -> {
                    val start = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val end = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    Pair(start, end)
                }
                DateFilterOption.THIS_WEEK -> {
                    val start = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val end = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 7)
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    Pair(start, end)
                }
                DateFilterOption.THIS_MONTH -> {
                    val start = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val end = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 30)
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    Pair(start, end)
                }
                DateFilterOption.NEXT_3_MONTHS -> {
                    val start = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val end = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 90)
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }
                    Pair(start, end)
                }
                else -> Pair(null, null)
            }

            Log.d("DateFilter", "📅 ========================================")
            Log.d("DateFilter", "📅 Filter: ${currentDateFilter.label}")
            Log.d("DateFilter", "📅 Start date: ${startDate?.time}")
            Log.d("DateFilter", "📅 End date: ${endDate?.time}")
            Log.d("DateFilter", "📅 Sorties before filter: ${list.size}")

            if (startDate != null && endDate != null) {
                list = list.filter { sortie ->
                    try {
                        val sortieDate = parseSortieDate(sortie.date)

                        if (sortieDate != null) {
                            val isInRange = sortieDate.time >= startDate.timeInMillis &&
                                           sortieDate.time <= endDate.timeInMillis

                            Log.d("DateFilter", "📅 '${sortie.titre}' - Date: ${sortie.date} -> Parsed: $sortieDate -> InRange: $isInRange")
                            isInRange
                        } else {
                            Log.d("DateFilter", "📅 '${sortie.titre}' - Could not parse date: ${sortie.date}, keeping")
                            false // Exclude sorties with unparseable dates from date filter
                        }
                    } catch (e: Exception) {
                        Log.e("DateFilter", "❌ Error for ${sortie.titre}: ${e.message}")
                        false
                    }
                }
            }

            Log.d("DateFilter", "📅 Sorties after filter: ${list.size}")
            Log.d("DateFilter", "📅 ========================================")
        }

        // ✅ NEW: Proximity filter
        if (currentProximityFilter != ProximityFilterOption.ALL &&
            currentProximityFilter.radiusKm != null &&
            currentUserLat != null &&
            currentUserLng != null) {

            val userLat = currentUserLat
            val userLng = currentUserLng
            val radiusKm = currentProximityFilter.radiusKm!!

            Log.d("ProximityFilter", "📍 User location: $userLat, $userLng")
            Log.d("ProximityFilter", "📍 Radius: $radiusKm km")

            list = list.filter { sortie ->
                val sortieLat = sortie.itineraire?.pointDepart?.latitude
                val sortieLng = sortie.itineraire?.pointDepart?.longitude

                if (sortieLat != null && sortieLng != null) {
                    val distance = calculateDistanceKm(userLat, userLng, sortieLat, sortieLng)
                    val isInRange = distance <= radiusKm
                    Log.d("ProximityFilter", "📍 Sortie '${sortie.titre}' at $sortieLat, $sortieLng - Distance: ${String.format("%.2f", distance)} km, inRange: $isInRange")
                    isInRange
                } else {
                    Log.d("ProximityFilter", "📍 Sortie '${sortie.titre}' has no coordinates, keeping it")
                    true // Keep sorties without coordinates
                }
            }

            Log.d("ProximityFilter", "📍 Filtered result: ${list.size} sorties")
        }

        Log.d("FilterDebug", "🔍 Final result: ${list.size} sorties")

        // Return sorted list
        list.sortedBy {
            try {
                parseSortieDate(it.date)?.time ?: Long.MAX_VALUE
            } catch (e: Exception) {
                Long.MAX_VALUE
            }
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) viewModel.refresh()
    }

    LaunchedEffect(viewModel.isLoading) {
        if (!viewModel.isLoading && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkMode) {
                        listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
                    } else {
                        listOf(BackgroundLightGradientStart, BackgroundLight, BackgroundLightGradientEnd)
                    }
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // === HEADER ===
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // ✅ Espacement augmenté pour éviter le chevauchement avec la TopBar
                Spacer(modifier = Modifier.height(56.dp))

                // Search Bar with Filter Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Bar
                    Box(modifier = Modifier.weight(1f).height(56.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            GreenAccent.copy(0.1f),
                                            TealAccent.copy(0.1f)
                                        )
                                    ),
                                    RoundedCornerShape(28.dp)
                                )
                                .blur(4.dp)
                        )
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(28.dp),
                            color = CardGlass,
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CardDark.copy(alpha = 0.3f))
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    "Search",
                                    tint = GreenAccent.copy(0.7f),
                                    modifier = Modifier.size(22.dp)
                                )
                                BasicTextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (viewModel.searchQuery.isEmpty()) {
                                            Text(
                                                "Search adventures...",
                                                color = TextSecondary,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                IconButton(onClick = {
                                    if (viewModel.searchQuery.isNotEmpty()) {
                                        viewModel.updateSearchQuery("")
                                    } else {
                                        try {
                                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                                putExtra(RecognizerIntent.EXTRA_PROMPT, "🎤 Say something to search...")
                                            }
                                            voiceSearchLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "❌ Voice search not available", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (viewModel.searchQuery.isEmpty()) Icons.Default.Mic else Icons.Default.Clear,
                                        contentDescription = if (viewModel.searchQuery.isEmpty()) "Voice Search" else "Clear",
                                        tint = GreenAccent.copy(0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ✅ NEW: Advanced Filter Button
                    Box {
                        Surface(
                            onClick = { showFilterSheet = true },
                            shape = CircleShape,
                            color = if (viewModel.hasActiveAdvancedFilters()) GreenAccent else CardDark.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (viewModel.hasActiveAdvancedFilters()) GreenAccent else BorderColor),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filtres avancés",
                                    tint = if (viewModel.hasActiveAdvancedFilters()) Color.White else GreenAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Badge indicator if filters active
                        if (viewModel.hasActiveAdvancedFilters()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(12.dp)
                                    .background(Color(0xFFFF5722), CircleShape)
                                    .border(2.dp, CardDark, CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FilterPill(
                            "Explore",
                            Icons.Default.Explore,
                            viewModel.selectedFilter == "explore"
                        ) { viewModel.setFilter("explore") }
                    }
                    item {
                        FilterPill(
                            "Recommended",
                            Icons.Default.Stars,
                            false
                        ) {
                            navController.navigate("flask_recommendations")
                        }
                    }
                    item {
                        FilterPill(
                            "Following",
                            Icons.Default.Favorite,
                            viewModel.selectedFilter == "following"
                        ) { viewModel.setFilter("following") }
                    }
                    item {
                        FilterPill(
                            "Cycling",
                            Icons.Default.DirectionsBike,
                            viewModel.selectedFilter == "cycling"
                        ) { viewModel.setFilter("cycling") }
                    }
                    item {
                        FilterPill(
                            "Hiking",
                            Icons.Default.Hiking,
                            viewModel.selectedFilter == "hiking"
                        ) { viewModel.setFilter("hiking") }
                    }
                    item {
                        FilterPill(
                            "Camping",
                            Icons.Default.Terrain,
                            viewModel.selectedFilter == "camping"
                        ) { viewModel.setFilter("camping") }
                    }
                }
            }

            // === CONTENT ===
            Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
                when {
                    viewModel.isLoading && viewModel.sorties.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = GreenAccent,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    "Chargement des aventures...",
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    filteredSorties.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (viewModel.selectedFilter == "following") Icons.Default.PeopleAlt else Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = if (viewModel.selectedFilter == "following")
                                        "Aucune sortie de tes abonnements"
                                    else "Aucune aventure trouvée",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (viewModel.selectedFilter == "following")
                                        "Suis des créateurs pour voir leurs sorties ici !"
                                    else "Essaie de changer de filtre ou de recherche",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // ✅ NEW: People Match Card - Elegant design
                            item {
                                PeopleMatchCard(
                                    onClick = { navController.navigate("people_recommendations") }
                                )
                            }

                            items(filteredSorties) { sortie ->
                                val isSaved = sortie.id in savedSortieIds
                                ModernEventCard(
                                    sortie = sortie,
                                    token = token,
                                    isFollowingCreator = sortie.createurId.id in followingIds,
                                    onClick = { navController.navigate("sortieDetail/${sortie.id}") },
                                    onUserClick = { navController.navigate("userProfile/${sortie.createurId.id}") },
                                    onBookmarkClick = {
                                        if (isSaved) {
                                            // Supprimer de la liste sauvegardée LOCALEMENT
                                            savedSortiesViewModel.removeSavedSortie(context, sortie.id)
                                        } else {
                                            // Ajouter à la liste sauvegardée LOCALEMENT
                                            savedSortiesViewModel.saveSortie(context, sortie)
                                        }
                                    },
                                    isBookmarked = isSaved
                                )
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }

                if (pullToRefreshState.isRefreshing || pullToRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = pullToRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }

        // ✅ NEW: Advanced Filters Bottom Sheet
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
                containerColor = CardDark,
                contentColor = TextPrimary,
                dragHandle = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        )
                    }
                }
            ) {
                AdvancedFiltersContent(
                    dateFilter = viewModel.dateFilter,
                    onDateFilterChange = { viewModel.updateDateFilter(it) },
                    proximityFilter = viewModel.proximityFilter,
                    onProximityFilterChange = { viewModel.updateProximityFilter(it) },
                    onClearAll = { viewModel.clearAdvancedFilters() },
                    onApply = { showFilterSheet = false },
                    onRequestLocation = {
                        // TODO: Request location permission and get current location
                        // For now, set a default Paris location for testing
                        viewModel.setUserLocation(48.8566, 2.3522)
                        Toast.makeText(context, "📍 Position mise à jour", Toast.LENGTH_SHORT).show()
                    },
                    hasLocation = viewModel.userLatitude != null
                )
            }
        }
    }
}

@Composable
fun ModernEventCard(
    sortie: SortieResponse,
    token: String,
    isFollowingCreator: Boolean = false,
    onClick: () -> Unit,
    onUserClick: ((String) -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false
) {
    val context = LocalContext.current

    fun formatDate(dateString: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        input.timeZone = TimeZone.getTimeZone("UTC")
        val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        input.parse(dateString)?.let { output.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }

    fun formatTime(dateString: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        input.timeZone = TimeZone.getTimeZone("UTC")
        val output = SimpleDateFormat("HH:mm", Locale.getDefault())
        input.parse(dateString)?.let { output.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }

    fun getDifficultyColor(type: String) = when (type) {
        "RANDONNEE" -> WarningOrange
        "VELO" -> TealAccent
        "CAMPING" -> SuccessGreen
        else -> TextTertiary
    }

    fun formatType(type: String) = when (type) {
        "RANDONNEE" -> "Hiking"
        "VELO" -> "Cycling"
        "CAMPING" -> "Camping"
        else -> type
    }

    fun getDefaultImage(type: String) = when (type) {
        "VELO" -> R.drawable.homme
        "RANDONNEE" -> R.drawable.jbal
        "CAMPING" -> R.drawable.camping
        else -> R.drawable.download
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardGlass,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CardDark.copy(0.4f),
                            CardDark.copy(0.6f)
                        )
                    )
                )
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
                if (sortie.photo?.isNotEmpty() == true) {
                    AsyncImage(
                        model = sortie.photo,
                        contentDescription = sortie.titre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(getDefaultImage(sortie.type)),
                        placeholder = painterResource(getDefaultImage(sortie.type))
                    )
                } else {
                    Image(
                        painter = painterResource(getDefaultImage(sortie.type)),
                        contentDescription = sortie.titre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(0.8f))
                        )
                    )
                )

                // Type Badge
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getDifficultyColor(sortie.type).copy(0.9f)
                    ) {
                        Text(
                            text = formatType(sortie.type),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Camping Option Badge
                if (sortie.optionCamping) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = SuccessGreen.copy(0.9f),
                        border = BorderStroke(1.dp, Color.White.copy(0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Terrain,
                                "Camping",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Camping",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Participants Badge
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(0.6f),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Group,
                            "Participants",
                            tint = GreenAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${sortie.participants.size}/${sortie.capacite}",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, GreenAccent.copy(0.5f), CircleShape)
                                .background(CardDark)
                                .clickable {
                                    onUserClick?.invoke(sortie.createurId.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // ✅ FETCH AVATAR FROM USER PROFILE (not from sortie)
                            val creatorAvatarUrl = remember(sortie.createurId.id) {
                                mutableStateOf<String?>(null)
                            }

                            // Fetch avatar from user profile when card is displayed
                            LaunchedEffect(sortie.createurId.id) {
                                Log.d("HomeExplore", "🔄 Fetching avatar for user ${sortie.createurId.id}")
                                val avatar = AvatarCache.getAvatarForUser(
                                    userId = sortie.createurId.id,
                                    token = token
                                )
                                creatorAvatarUrl.value = avatar
                                Log.d("HomeExplore", "✅ Got avatar: $avatar")
                            }

                            // Display the fetched avatar
                            UserAvatar(
                                avatarUrl = creatorAvatarUrl.value,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Following badge
                            if (isFollowingCreator) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp)
                                        .size(20.dp)
                                        .background(SuccessGreen, CircleShape)
                                        .border(2.dp, CardDark, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        "Suivi",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                sortie.titre,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    "Location",
                                    tint = GreenAccent.copy(0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = sortie.itineraire?.pointArrivee?.address?.takeIf { it.isNotEmpty() }
                                        ?: sortie.itineraire?.pointArrivee?.displayName?.takeIf { it.isNotEmpty() }
                                        ?: "Lieu inconnu",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    IconButton(onClick = { onBookmarkClick?.invoke() }) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isBookmarked) "Saved" else "Save",
                            tint = if (isBookmarked) GreenAccent else GreenAccent.copy(0.6f)
                        )
                    }
                }

                HorizontalDivider(color = BorderColor, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                formatDate(sortie.date),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                formatTime(sortie.date),
                                color = TextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(BorderColor))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Route,
                            "Distance",
                            tint = GreenAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "${sortie.itineraire?.distance?.div(1000) ?: 0} km",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ✅ NEW: Elegant People Match Card - iOS inspired design
@Composable
fun PeopleMatchCard(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = GreenAccent.copy(alpha = 0.1f),
                spotColor = GreenAccent.copy(alpha = 0.15f)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            GreenAccent.copy(alpha = 0.3f),
                            TealAccent.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon container with gradient background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        GreenAccent.copy(alpha = 0.2f),
                                        TealAccent.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Find People",
                            tint = GreenAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Find Your Adventure Friends",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Discover people who match your profile",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Arrow icon with subtle background
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = GreenAccent.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = GreenAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) GreenAccent.copy(0.2f) else CardGlass,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) GreenAccent else BorderColor
        ),
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier
                .background(
                    if (isSelected)
                        Brush.horizontalGradient(
                            listOf(
                                GreenAccent.copy(0.15f),
                                TealAccent.copy(0.15f)
                            )
                        )
                    else
                        Brush.horizontalGradient(
                            listOf(
                                CardDark.copy(0.3f),
                                CardDark.copy(0.3f)
                            )
                        )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = if (isSelected) GreenAccent else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = if (isSelected) GreenAccent else TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

class UserProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// ✅ NEW: Advanced Filters Content - iOS Style
@Composable
fun AdvancedFiltersContent(
    dateFilter: DateFilterOption,
    onDateFilterChange: (DateFilterOption) -> Unit,
    proximityFilter: ProximityFilterOption,
    onProximityFilterChange: (ProximityFilterOption) -> Unit,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
    onRequestLocation: () -> Unit,
    hasLocation: Boolean
) {
    // State for filter mode (date or proximity)
    var showDateFilter by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(50f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header with toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showDateFilter) "Filtrer par date" else "Filtrer par localisation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            TextButton(onClick = { showDateFilter = !showDateFilter }) {
                Text(
                    text = if (showDateFilter) "📍 Proximité" else "📅 Date",
                    color = GreenAccent,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showDateFilter) {
            // ==================== DATE FILTER - iOS STYLE ====================
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterOption.values().forEach { option ->
                    DateFilterCard(
                        option = option,
                        isSelected = dateFilter == option,
                        onClick = { onDateFilterChange(option) }
                    )
                }
            }
        } else {
            // ==================== PROXIMITY FILTER - iOS STYLE ====================
            // Ma position card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardDark.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Navigation,
                            contentDescription = "Position",
                            tint = GreenAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Ma position",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasLocation) "Position activée" else "Position non définie",
                                fontSize = 12.sp,
                                color = if (hasLocation) GreenAccent else TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onRequestLocation) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = GreenAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rayon de recherche
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardDark.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Rayon",
                            tint = GreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Rayon de recherche",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${sliderValue.toInt()} km",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            // Map slider to proximity filter
                            val newFilter = when {
                                it <= 5 -> ProximityFilterOption.NEARBY_5
                                it <= 10 -> ProximityFilterOption.NEARBY_10
                                it <= 25 -> ProximityFilterOption.NEARBY_25
                                it <= 50 -> ProximityFilterOption.NEARBY_50
                                it <= 100 -> ProximityFilterOption.NEARBY_100
                                else -> ProximityFilterOption.ALL
                            }
                            onProximityFilterChange(newFilter)
                        },
                        valueRange = 5f..200f,
                        colors = SliderDefaults.colors(
                            thumbColor = GreenAccent,
                            activeTrackColor = GreenAccent,
                            inactiveTrackColor = BorderColor
                        ),
                        enabled = hasLocation
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("5 km", fontSize = 12.sp, color = TextSecondary)
                        Text("200 km", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Raccourcis
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardDark.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Raccourcis",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 25, 50, 100).forEach { km ->
                            Surface(
                                onClick = {
                                    sliderValue = km.toFloat()
                                    val newFilter = when (km) {
                                        10 -> ProximityFilterOption.NEARBY_10
                                        25 -> ProximityFilterOption.NEARBY_25
                                        50 -> ProximityFilterOption.NEARBY_50
                                        100 -> ProximityFilterOption.NEARBY_100
                                        else -> ProximityFilterOption.ALL
                                    }
                                    onProximityFilterChange(newFilter)
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = if (sliderValue.toInt() == km) GreenAccent else Color.Transparent,
                                border = BorderStroke(1.dp, if (sliderValue.toInt() == km) GreenAccent else BorderColor),
                                enabled = hasLocation
                            ) {
                                Text(
                                    text = "${km}km",
                                    fontSize = 13.sp,
                                    color = if (sliderValue.toInt() == km) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reset button
            OutlinedButton(
                onClick = onClearAll,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "Réinitialiser",
                    color = TextPrimary,
                    fontSize = 14.sp
                )
            }

            // Apply button
            Button(
                onClick = onApply,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text(
                    text = "Appliquer",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ✅ Date Filter Card - iOS Style
@Composable
fun DateFilterCard(
    option: DateFilterOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val description = when (option) {
        DateFilterOption.ALL -> "Toutes les sorties disponibles"
        DateFilterOption.TODAY -> "Sorties prévues aujourd'hui"
        DateFilterOption.THIS_WEEK -> "Sorties de cette semaine"
        DateFilterOption.THIS_MONTH -> "Sorties de ce mois"
        DateFilterOption.NEXT_3_MONTHS -> "Toutes les sorties à venir"
    }

    val icon = when (option) {
        DateFilterOption.ALL -> Icons.Default.DateRange
        DateFilterOption.TODAY -> Icons.Default.Today
        DateFilterOption.THIS_WEEK -> Icons.Default.ViewWeek
        DateFilterOption.THIS_MONTH -> Icons.Default.CalendarMonth
        DateFilterOption.NEXT_3_MONTHS -> Icons.Default.EventAvailable
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) GreenAccent.copy(alpha = 0.15f) else CardDark.copy(alpha = 0.5f),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) GreenAccent else BorderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = option.label,
                    tint = if (isSelected) GreenAccent else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = option.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) GreenAccent else TextPrimary
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun AdvancedFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = when {
            isSelected -> GreenAccent
            !enabled -> CardDark.copy(alpha = 0.3f)
            else -> CardDark.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            1.dp,
            when {
                isSelected -> GreenAccent
                !enabled -> BorderColor.copy(alpha = 0.3f)
                else -> BorderColor
            }
        )
    ) {
        Text(
            text = label,
            color = when {
                isSelected -> Color.White
                !enabled -> TextSecondary.copy(alpha = 0.5f)
                else -> TextSecondary
            },
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

// ✅ NEW: Calculate distance between two coordinates in kilometers
fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Earth's radius in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

    return earthRadius * c
}

// ✅ NEW: Parse sortie date with multiple format support
fun parseSortieDate(dateString: String?): Date? {
    if (dateString.isNullOrBlank()) return null

    val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    )

    for (format in dateFormats) {
        try {
            format.timeZone = TimeZone.getTimeZone("UTC")
            val result = format.parse(dateString)
            if (result != null) return result
        } catch (e: Exception) {
            // Try next format
        }
    }
    return null
}

