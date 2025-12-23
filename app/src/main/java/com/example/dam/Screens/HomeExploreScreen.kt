package com.example.dam.Screens

import android.content.Context
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
import coil.request.ImageRequest
import com.example.dam.R
import com.example.dam.models.SortieResponse
import com.example.dam.ui.theme.*
import com.example.dam.utils.UserPreferences
import com.example.dam.viewmodel.HomeExploreViewModel
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
    val token = UserPreferences.getToken(context) ?: ""
    val currentUserId = UserPreferences.getUserId(context) ?: ""

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

    val filteredSorties = remember(viewModel.sorties, viewModel.selectedFilter, viewModel.searchQuery, followingIds) {
        var list = viewModel.sorties.filter {
            it.titre.contains(viewModel.searchQuery, ignoreCase = true) ||
                    it.itineraire?.pointArrivee?.displayName?.contains(viewModel.searchQuery, ignoreCase = true) == true
        }

        list = when (viewModel.selectedFilter) {
            "following" -> if (followingIds.isNotEmpty()) {
                list.filter { it.createurId.id in followingIds }
            } else list
            "cycling" -> list.filter { it.type == "VELO" }
            "hiking" -> list.filter { it.type == "RANDONNEE" }
            "camping" -> list.filter { it.optionCamping }
            else -> list
        }

        list.sortedBy {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(it.date)?.time ?: Long.MAX_VALUE
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
                    colors = listOf(BackgroundGradientStart, BackgroundDark, BackgroundGradientEnd)
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
                Spacer(modifier = Modifier.height(24.dp))

                // Search Bar
                Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
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
                                if (viewModel.searchQuery.isNotEmpty()) viewModel.updateSearchQuery(
                                    ""
                                )
                            }) {
                                Icon(
                                    imageVector = if (viewModel.searchQuery.isEmpty()) Icons.Default.Mic else Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = GreenAccent.copy(0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
                            navController.navigate("recommendation_hub")
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
                            items(filteredSorties) { sortie ->
                                val isSaved = sortie.id in savedSortieIds
                                ModernEventCard(
                                    sortie = sortie,
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
    }
}

@Composable
fun ModernEventCard(
    sortie: SortieResponse,
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
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            GreenAccent,
                                            TealAccent
                                        )
                                    )
                                )
                                .clickable {
                                    onUserClick?.invoke(sortie.createurId.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            val avatar = sortie.createurId.avatar
                            if (avatar?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(avatar)
                                        .crossfade(true).build(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(R.drawable.homme),
                                    placeholder = painterResource(R.drawable.homme)
                                )
                            } else {
                                Text(
                                    text = sortie.createurId.email.firstOrNull()?.uppercaseChar()
                                        ?.toString() ?: "?",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

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