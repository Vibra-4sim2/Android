package com.example.dam.Screens

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.dam.ui.theme.CardDark
import com.example.dam.ui.theme.SuccessGreen
import com.example.dam.viewmodel.AddPublicationViewModel
import com.example.dam.viewmodel.AddPublicationUiState

// Couleurs - Palette élégante et minimaliste
private val BackgroundDark = Color(0xFF0A0A0A)
private val CardBackground = Color(0xFF1C1C1E)
private val CardBackgroundLight = Color(0xFF2C2C2E)
private val GreenAccent = Color(0xFF4ADE80)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)
private val TextTertiary = Color(0xFF636366)
private val RedAccent = Color(0xFFFF3B30)
private val DividerColor = Color(0xFF38383A)

// ==================== UI PRINCIPALE ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPublicationScreen(navController: NavHostController) {
    val context = LocalContext.current

    // ✅ UTILISE LE BON VIEWMODEL (celui avec UserPreferences)
    val viewModel: AddPublicationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AddPublicationViewModel(
                    context.applicationContext as Application
                ) as T
            }
        }
    )

    // Collecter les states
    val uiState by viewModel.uiState.collectAsState()
    val contentText by viewModel.content.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val mentionedUsers by viewModel.mentionedUsers.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()

    var showMentionDialog by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }

    // 🆕 Récupérer les informations de l'utilisateur connecté
    var userName by remember { mutableStateOf("Loading...") }
    var userAvatar by remember { mutableStateOf<String?>(null) }
    var userInitials by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val userId = com.example.dam.utils.UserPreferences.getUserId(context)
        val token = com.example.dam.utils.UserPreferences.getToken(context)

        if (userId != null && token != null) {
            try {
                val apiService = com.example.dam.remote.RetrofitInstance.authApi
                val response = apiService.getUserById(userId, "Bearer $token")

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    userName = "${user.firstName} ${user.lastName}"
                    userAvatar = user.avatar
                    userInitials = "${user.firstName.firstOrNull() ?: ""}${user.lastName.firstOrNull() ?: ""}".uppercase()
                    Log.d("AddPublicationScreen", "✅ User loaded: $userName")
                } else {
                    userName = "Your Name"
                    userInitials = "YO"
                }
            } catch (e: Exception) {
                Log.e("AddPublicationScreen", "❌ Error loading user", e)
                userName = "Your Name"
                userInitials = "YO"
            }
        }
    }

    // 🚨 Navigation automatique après succès
    LaunchedEffect(uiState) {
        Log.d("AddPublicationScreen", "🔄 UI State changed: $uiState")
        when (val state = uiState) {
            is AddPublicationUiState.Success -> {
                Log.d("AddPublicationScreen", "✅ SUCCESS! Publication ID: ${state.publicationId}")
                Log.d("AddPublicationScreen", "🚀 Navigating to feed...")

                // Petit délai pour laisser l'UI se stabiliser
                kotlinx.coroutines.delay(300)

                // Navigation simple vers feed en supprimant addpublication du backstack
                navController.navigate("feed") {
                    // Supprimer TOUT jusqu'à home, puis aller à feed
                    popUpTo("home") {
                        inclusive = false
                    }
                    launchSingleTop = true
                }

                // Reset l'état après navigation
                kotlinx.coroutines.delay(100)
                viewModel.resetUiState()
            }
            is AddPublicationUiState.Error -> {
                Log.e("AddPublicationScreen", "❌ Error: ${state.message}")
            }
            is AddPublicationUiState.Loading -> {
                Log.d("AddPublicationScreen", "⏳ Loading...")
            }
            is AddPublicationUiState.Idle -> {
                Log.d("AddPublicationScreen", "💤 Idle")
            }
        }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.selectImage(uri)
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            AddPublicationTopBar(
                onBackClick = { navController.popBackStack() },
                onPostClick = {
                    if (contentText.isNotBlank()) {
                        viewModel.publishPublication()
                    }
                },
                isPostEnabled = contentText.isNotBlank(),
                isPosting = uiState is AddPublicationUiState.Loading
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Avatar + Nom - Design amélioré avec vraies données utilisateur
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .fillMaxWidth()
            ) {
                // Avatar cliquable → Profile
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    GreenAccent.copy(alpha = 0.3f),
                                    GreenAccent.copy(alpha = 0.1f)
                                )
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, GreenAccent.copy(alpha = 0.4f), CircleShape)
                        .clickable {
                            // Navigation vers le profil
                            navController.navigate("profile")
                        }
                        .padding(2.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = CardBackgroundLight
                    ) {
                        if (userAvatar != null && userAvatar!!.isNotEmpty()) {
                            // TODO: Charger l'image avec Coil si vous avez la lib
                            // AsyncImage(model = userAvatar, ...)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = userInitials,
                                    color = GreenAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = userInitials,
                                    color = GreenAccent,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = userName,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Public post",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Zone de texte - Design moderne et minimaliste
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            CardBackground.copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(18.dp)
                ) {
                    TextField(
                        value = contentText,
                        onValueChange = { viewModel.updateContent(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "What's on your mind?\nShare your cycling story, tips, or adventures...",
                                color = TextSecondary.copy(alpha = 0.6f),
                                lineHeight = 22.sp,
                                fontSize = 15.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = GreenAccent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }

            // Image preview - Design amélioré
            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(20.dp))
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            CardBackgroundLight.copy(alpha = 0.6f),
                                            CardBackgroundLight.copy(alpha = 0.4f)
                                        )
                                    ),
                                    RoundedCornerShape(24.dp)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = GreenAccent.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    GreenAccent.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            ),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Image,
                                        contentDescription = "Selected image",
                                        tint = GreenAccent,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Image Selected",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ready to share",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.selectImage(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(38.dp)
                            .shadow(8.dp, CircleShape)
                            .background(RedAccent, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Tags sélectionnés
            if (selectedTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(selectedTags) { tag ->
                        TagChip(
                            tag = tag,
                            onRemove = { viewModel.removeTag(tag) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions - Design moderne avec 3 boutons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AddActionButton(
                    icon = Icons.Outlined.Image,
                    label = "Photo",
                    color = Color(0xFF3B82F6),
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )

                AddActionButton(
                    icon = Icons.Outlined.Tag,
                    label = "Tags",
                    color = Color(0xFFF59E0B),
                    onClick = { showTagDialog = true },
                    modifier = Modifier.weight(1f)
                )

                AddActionButton(
                    icon = Icons.Outlined.AlternateEmail,
                    label = "Mention",
                    color = GreenAccent,
                    onClick = { /* TODO: Désactivé pour l'instant */ },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bouton Publish - Design moderne et élégant
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                onClick = {
                    Log.d("AddPublication", "🔵 PUBLISH BUTTON CLICKED!")
                    Log.d("AddPublication", "Content: '$contentText'")
                    Log.d("AddPublication", "Current state: $uiState")
                    if (contentText.isNotBlank() && uiState !is AddPublicationUiState.Loading) {
                        Log.d("AddPublication", "✅ Calling viewModel.publishPublication()")
                        viewModel.publishPublication()
                    } else {
                        Log.d("AddPublication", "❌ Cannot publish: content blank or already loading")
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (contentText.isNotBlank() && uiState !is AddPublicationUiState.Loading) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        GreenAccent,
                                        GreenAccent.copy(alpha = 0.85f)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        GreenAccent.copy(alpha = 0.3f),
                                        GreenAccent.copy(alpha = 0.25f)
                                    )
                                )
                            },
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState is AddPublicationUiState.Loading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Publishing...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Publish on Feed",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TipsCard()
        }

        // Dialogs
        if (showTagDialog) {
            TagSelectionDialog(
                onDismiss = { showTagDialog = false },
                onTagsSelected = { tags ->
                    viewModel.setTags(tags)
                    showTagDialog = false
                }
            )
        }
    }
}

// ==================== COMPOSANTS UI ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPublicationTopBar(
    onBackClick: () -> Unit,
    onPostClick: () -> Unit,
    isPostEnabled: Boolean,
    isPosting: Boolean
) {
    Surface(
        color = BackgroundDark,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding() // Évite le notch
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Bouton X à gauche - BIEN VISIBLE
                Surface(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(42.dp)
                        .shadow(8.dp, CircleShape),
                    shape = CircleShape,
                    color = CardBackgroundLight
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(
                                width = 2.dp,
                                color = RedAccent.copy(alpha = 0.4f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = RedAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Titre centré - BIEN VISIBLE
                Text(
                    text = "New Post",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Divider élégant
            HorizontalDivider(
                color = DividerColor.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun AddActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(80.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.20f),
                            color.copy(alpha = 0.10f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = color.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    color = color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TagChip(tag: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier.shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GreenAccent.copy(alpha = 0.25f),
                            GreenAccent.copy(alpha = 0.15f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = GreenAccent.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$tag",
                    color = GreenAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = GreenAccent,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove() }
                )
            }
        }
    }
}

@Composable
fun TipsCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            CardBackground.copy(alpha = 0.5f),
                            CardBackground.copy(alpha = 0.3f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFFBBF24).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFBBF24).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Pro Tips",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Use hashtags to reach more cyclists\n• Add photos to make your post engaging",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TagSelectionDialog(onDismiss: () -> Unit, onTagsSelected: (List<String>) -> Unit) {
    val availableTags = listOf("Cycling", "Training", "Mountains", "RoadBike", "Fitness", "Adventure")
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text("Select Tags", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                availableTags.chunked(3).forEach { rowTags ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTags.forEach { tag ->
                            FilterChip(
                                selected = tag in selectedTags,
                                onClick = {
                                    selectedTags = if (tag in selectedTags) {
                                        selectedTags - tag
                                    } else {
                                        selectedTags + tag
                                    }
                                },
                                label = { Text(tag) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenAccent.copy(alpha = 0.3f),
                                    selectedLabelColor = GreenAccent
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onTagsSelected(selectedTags) }) {
                Text("Done", color = GreenAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}