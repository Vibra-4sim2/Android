package com.example.dam.Screens

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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

// Couleurs
private val BackgroundDark = Color(0xFF0F0F0F)
private val CardBackground = Color(0xFF1A1A1A)
private val GreenAccent = Color(0xFF4ADE80)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF9CA3AF)
private val TextTertiary = Color(0xFF6B7280)   // 👈 AJOUTER

private val RedAccent = Color(0xFFEF4444)

// ==================== UI PRINCIPALE ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPublicationScreen(navController: NavHostController) {
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }


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


    // Observe the UI state for success
    LaunchedEffect(uiState) {
        if (uiState is AddPublicationUiState.Success) {
            showSuccessDialog = true
        }
    }

    // Gérer les états de succès/erreur
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AddPublicationUiState.Success -> {
                Log.d("AddPublication", "✅ Success! Navigating back...")
                navController.popBackStack()
            }
            is AddPublicationUiState.Error -> {
                Log.e("AddPublication", "❌ Error: ${state.message}")
            }
            else -> {}
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
            // Avatar + Nom
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .border(2.dp, GreenAccent.copy(alpha = 0.3f), CircleShape)
                        .padding(3.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color(0xFF374151)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "YO",
                                color = GreenAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Your Name",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Public post",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Zone de texte
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                shape = RoundedCornerShape(20.dp),
                color = CardBackground.copy(alpha = 0.6f)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A1A).copy(alpha = 0.8f),
                                    Color(0xFF151515).copy(alpha = 0.6f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    TextField(
                        value = contentText,
                        onValueChange = { viewModel.updateContent(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "What's on your mind? Share your cycling journey...",
                                color = TextSecondary.copy(alpha = 0.6f)
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
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            // Image preview
            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(16.dp))
                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF374151)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = "Selected image",
                                    tint = GreenAccent,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Image Selected",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.selectImage(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(RedAccent, CircleShape)
                            .shadow(4.dp, CircleShape)
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

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AddActionButton(
                    icon = Icons.Outlined.Image,
                    label = "Photo",
                    color = Color(0xFF3B82F6),
                    onClick = { imagePickerLauncher.launch("image/*") }
                )

                AddActionButton(
                    icon = Icons.Outlined.Tag,
                    label = "Tag",
                    color = Color(0xFFF59E0B),
                    onClick = { showTagDialog = true }
                )

                AddActionButton(
                    icon = Icons.Outlined.AlternateEmail,
                    label = "Mention",
                    color = GreenAccent,
                    onClick = { /* TODO: Désactivé pour l'instant */ }
                )

                AddActionButton(
                    icon = Icons.Outlined.LocationOn,
                    label = "Location",
                    color = Color(0xFFEF4444),
                    onClick = { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bouton Publish
            Button(
                onClick = {
                    if (contentText.isNotBlank()) {
                        viewModel.publishPublication()
                    }
                },
                enabled = contentText.isNotBlank() && uiState !is AddPublicationUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenAccent,
                    disabledContainerColor = GreenAccent.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (uiState is AddPublicationUiState.Loading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Publishing...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Publish on Feed",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            // Success Dialog - Add this at the bottom of your composable, before the closing }
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showSuccessDialog = false
                        navController.navigate("feed") {
                            popUpTo("addpublication") { inclusive = true }
                        }
                    },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            SuccessGreen.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Published Successfully!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            text = "Your post has been shared with the community. Check it out in your feed!",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                navController.navigate("feed") {
                                    popUpTo("addpublication") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("View in Feed", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showSuccessDialog = false
                                navController.popBackStack()
                            }
                        ) {
                            Text("Stay Here", color = TextTertiary)
                        }
                    },
                    containerColor = CardDark,
                    shape = RoundedCornerShape(24.dp)
                )
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
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }

            Text(
                text = "New Post",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun AddActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(75.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TagChip(tag: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = GreenAccent.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, GreenAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$tag",
                color = GreenAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
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

@Composable
fun TipsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = Color(0xFFFBBF24),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Pro Tips",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Use hashtags to reach more cyclists\n• Add photos to make your post engaging",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
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