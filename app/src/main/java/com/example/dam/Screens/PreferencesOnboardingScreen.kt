

package com.example.dam.Screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.dam.R
import com.example.dam.models.OnboardingPreferencesRequest
import com.example.dam.remote.RetrofitInstance
import com.example.dam.ui.theme.DamTheme
import com.example.dam.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PreferencesOnboardingScreen(navController: NavController) {
    val context = LocalContext.current
    var currentSection by remember { mutableStateOf(0) }
    val greenColor = Color(0xFF4CAF50)
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Récupérer userId depuis SharedPreferences
    val userId = remember { UserPreferences.getUserId(context) }

    // État pour stocker les préférences
    var level by remember { mutableStateOf<String?>(null) }

    // Vélo
    var cyclingType by remember { mutableStateOf<String?>(null) }
    var cyclingFrequency by remember { mutableStateOf<String?>(null) }
    var cyclingDistance by remember { mutableStateOf<String?>(null) }
    var cyclingGroupInterest by remember { mutableStateOf(false) }

    // Randonnée
    var hikeType by remember { mutableStateOf<String?>(null) }
    var hikeDuration by remember { mutableStateOf<String?>(null) }
    var hikePreference by remember { mutableStateOf<String?>(null) }

    // Camping
    var campingPractice by remember { mutableStateOf(false) }
    var campingType by remember { mutableStateOf<String?>(null) }
    var campingDuration by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            // Back Button
            IconButton(
                onClick = {
                    if (currentSection > 0) {
                        currentSection--
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= currentSection) greenColor
                                else Color.White.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Contenu selon la section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                when (currentSection) {
                    0 -> CyclingSection(
                        level = level,
                        cyclingType = cyclingType,
                        cyclingFrequency = cyclingFrequency,
                        cyclingDistance = cyclingDistance,
                        cyclingGroupInterest = cyclingGroupInterest,
                        onLevelChange = { level = it },
                        onTypeChange = { cyclingType = it },
                        onFrequencyChange = { cyclingFrequency = it },
                        onDistanceChange = { cyclingDistance = it },
                        onGroupInterestChange = { cyclingGroupInterest = it },
                        greenColor = greenColor
                    )
                    1 -> HikingSection(
                        hikeType = hikeType,
                        hikeDuration = hikeDuration,
                        hikePreference = hikePreference,
                        onTypeChange = { hikeType = it },
                        onDurationChange = { hikeDuration = it },
                        onPreferenceChange = { hikePreference = it },
                        greenColor = greenColor
                    )
                    2 -> CampingSection(
                        campingPractice = campingPractice,
                        campingType = campingType,
                        campingDuration = campingDuration,
                        onPracticeChange = { campingPractice = it },
                        onTypeChange = { campingType = it },
                        onDurationChange = { campingDuration = it },
                        greenColor = greenColor
                    )
                }
            }

            // Message d'erreur
            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            // Bottom Section avec bouton Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp, top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (currentSection == 0) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Spacer(modifier = Modifier.size(80.dp))
                }

                // Next Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoading) Color.White.copy(alpha = 0.1f)
                            else Color.White.copy(alpha = 0.2f)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .clickable(enabled = !isLoading) {
                            if (currentSection < 2) {
                                currentSection++
                                errorMessage = null
                            } else {
                                // Vérifier que userId existe
                                if (userId == null) {
                                    errorMessage = "Erreur: Utilisateur non connecté"
                                    return@clickable
                                }

                                // Enregistrer les préférences
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val success = savePreferences(
                                        context = context,
                                        userId = userId,
                                        level = level,
                                        cyclingType = cyclingType,
                                        cyclingFrequency = cyclingFrequency,
                                        cyclingDistance = cyclingDistance,
                                        cyclingGroupInterest = cyclingGroupInterest,
                                        hikeType = hikeType,
                                        hikeDuration = hikeDuration,
                                        hikePreference = hikePreference,
                                        campingPractice = campingPractice,
                                        campingType = campingType,
                                        campingDuration = campingDuration
                                    )
                                    isLoading = false

                                    if (success) {
                                        UserPreferences.setOnboardingComplete(context, true)
                                        navController.navigate("home") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = "Erreur lors de l'enregistrement"
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                if (currentSection == 2) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Spacer(modifier = Modifier.size(80.dp))
                }
            }
        }
    }
}

@Composable
fun CyclingSection(
    level: String?,
    cyclingType: String?,
    cyclingFrequency: String?,
    cyclingDistance: String?,
    cyclingGroupInterest: Boolean,
    onLevelChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onGroupInterestChange: (Boolean) -> Unit,
    greenColor: Color
) {
    Text(
        text = "Vélo 🚴",
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Parlez-nous de vos habitudes cyclistes",
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Niveau
    SectionTitle("Quel est votre niveau ?")
    Spacer(modifier = Modifier.height(12.dp))

    listOf(
        "BEGINNER" to "Débutant",
        "INTERMEDIATE" to "Intermédiaire",
        "ADVANCED" to "Avancé"
    ).forEach { (value, label) ->
        PreferenceOption(
            text = label,
            isSelected = level == value,
            onClick = { onLevelChange(value) },
            greenColor = greenColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Type de vélo
    SectionTitle("Type de vélo préféré ?")
    Spacer(modifier = Modifier.height(12.dp))

    listOf(
        "VTT" to "VTT 🚵",
        "ROUTE" to "Route 🚴",
        "GRAVEL" to "Gravel 🚲",
        "URBAIN" to "Urbain 🚴‍♀️",
        "ELECTRIQUE" to "Électrique ⚡"
    ).forEach { (value, label) ->
        PreferenceOption(
            text = label,
            isSelected = cyclingType == value,
            onClick = { onTypeChange(value) },
            greenColor = greenColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Fréquence
    SectionTitle("À quelle fréquence roulez-vous ?")
    Spacer(modifier = Modifier.height(12.dp))

    listOf(
        "QUOTIDIEN" to "Quotidiennement",
        "HEBDO" to "Plusieurs fois par semaine",
        "WEEKEND" to "Le week-end",
        "RARE" to "Rarement"
    ).forEach { (value, label) ->
        PreferenceOption(
            text = label,
            isSelected = cyclingFrequency == value,
            onClick = { onFrequencyChange(value) },
            greenColor = greenColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Distance
    SectionTitle("Distance moyenne par sortie ?")
    Spacer(modifier = Modifier.height(12.dp))

    listOf(
        "<10" to "Moins de 10 km",
        "10-30" to "10-30 km",
        "30-60" to "30-60 km",
        ">60" to "Plus de 60 km"
    ).forEach { (value, label) ->
        PreferenceOption(
            text = label,
            isSelected = cyclingDistance == value,
            onClick = { onDistanceChange(value) },
            greenColor = greenColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Sortie en groupe
    SectionTitle("Intéressé par les sorties en groupe ?")
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreferenceOption(
            text = "Oui",
            isSelected = cyclingGroupInterest,
            onClick = { onGroupInterestChange(true) },
            greenColor = greenColor,
            modifier = Modifier.weight(1f)
        )
        PreferenceOption(
            text = "Non",
            isSelected = !cyclingGroupInterest,
            onClick = { onGroupInterestChange(false) },
            greenColor = greenColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HikingSection(
    hikeType: String?,
    hikeDuration: String?,
    hikePreference: String?,
    onTypeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onPreferenceChange: (String) -> Unit,
    greenColor: Color
) {
    Text(
        text = "Randonnée ⛰️",
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Vos préférences de randonnée",
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Type de randonnée
    SectionTitle("Type de randonnée préféré ?")
    Spacer(modifier = Modifier.height(12.dp))

    listOf(
        "COURTE" to "Courte (balades)",
        "MONTAGNE" to "Montagne",
        "LONGUE" to "Longue distance",
        "TREKKING" to "Trekking"
    ).forEach { (value, label) ->
        PreferenceOption(
            text = label,
            isSelected = hikeType == value,
            onClick = { onTypeChange(value) },
            greenColor = greenColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Durée
    SectionTitle("Durée habituelle ?")
    Spacer(modifier = Modifier.height(12.dp))

    listOf(
        "<2H" to "Moins de 2h",
        "2-4H" to "2-4 heures",
        "4-8H" to "4-8 heures",
        ">8H" to "Plus de 8 heures"
    ).forEach { (value, label) ->
        PreferenceOption(
            text = label,
            isSelected = hikeDuration == value,
            onClick = { onDurationChange(value) },
            greenColor = greenColor
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Préférence groupe/seul
    SectionTitle("Vous préférez randonner ?")
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreferenceOption(
            text = "En groupe",
            isSelected = hikePreference == "GROUPE",
            onClick = { onPreferenceChange("GROUPE") },
            greenColor = greenColor,
            modifier = Modifier.weight(1f)
        )
        PreferenceOption(
            text = "Seul(e)",
            isSelected = hikePreference == "SEUL",
            onClick = { onPreferenceChange("SEUL") },
            greenColor = greenColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CampingSection(
    campingPractice: Boolean,
    campingType: String?,
    campingDuration: String?,
    onPracticeChange: (Boolean) -> Unit,
    onTypeChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    greenColor: Color
) {
    Text(
        text = "Camping 🏕️",
        color = Color.White,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "Vos habitudes de camping",
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Pratique du camping
    SectionTitle("Pratiquez-vous le camping ?")
    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PreferenceOption(
            text = "Oui",
            isSelected = campingPractice,
            onClick = { onPracticeChange(true) },
            greenColor = greenColor,
            modifier = Modifier.weight(1f)
        )
        PreferenceOption(
            text = "Non",
            isSelected = !campingPractice,
            onClick = { onPracticeChange(false) },
            greenColor = greenColor,
            modifier = Modifier.weight(1f)
        )
    }

    if (campingPractice) {
        Spacer(modifier = Modifier.height(24.dp))

        // Type de camping
        SectionTitle("Type de camping préféré ?")
        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            "TENTE" to "Tente ⛺",
            "VAN" to "Van 🚐",
            "CAMPING-CAR" to "Camping-car 🚙",
            "REFUGE" to "Refuge 🏠",
            "BIVOUAC" to "Bivouac 🏔️"
        ).forEach { (value, label) ->
            PreferenceOption(
                text = label,
                isSelected = campingType == value,
                onClick = { onTypeChange(value) },
                greenColor = greenColor
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Durée
        SectionTitle("Durée habituelle ?")
        Spacer(modifier = Modifier.height(12.dp))

        listOf(
            "1NUIT" to "1 nuit",
            "WEEKEND" to "Week-end",
            "3-5J" to "3-5 jours",
            ">1SEMAINE" to "Plus d'une semaine"
        ).forEach { (value, label) ->
            PreferenceOption(
                text = label,
                isSelected = campingDuration == value,
                onClick = { onDurationChange(value) },
                greenColor = greenColor
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun PreferenceOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    greenColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (isSelected) greenColor.copy(alpha = 0.2f)
                else Color.White.copy(alpha = 0.05f)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) greenColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) greenColor else Color.White,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

suspend fun savePreferences(
    context: Context,
    userId: String,
    level: String?,
    cyclingType: String?,
    cyclingFrequency: String?,
    cyclingDistance: String?,
    cyclingGroupInterest: Boolean,
    hikeType: String?,
    hikeDuration: String?,
    hikePreference: String?,
    campingPractice: Boolean,
    campingType: String?,
    campingDuration: String?
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val request = OnboardingPreferencesRequest(
                level = level,
                cyclingType = cyclingType,
                cyclingFrequency = cyclingFrequency,
                cyclingDistance = cyclingDistance,
                cyclingGroupInterest = if (cyclingGroupInterest) true else null,
                hikeType = hikeType,
                hikeDuration = hikeDuration,
                hikePreference = hikePreference,
                campingPractice = if (campingPractice) true else null,
                campingType = campingType,
                campingDuration = campingDuration
            )

            val token = "Bearer ${UserPreferences.getToken(context)}"


            Log.d("PreferencesAPI", "Sending request for userId: $userId")
            Log.d("PreferencesAPI", "Request body: $request")

            val response = RetrofitInstance.authApi.submitOnboardingPreferences(
                userId = userId,
                preferences = request,
                token = token
            )

            if (response.isSuccessful) {
                Log.d("PreferencesAPI", "Success: ${response.body()}")
                true
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("PreferencesAPI", "Error ${response.code()}: $errorBody")
                false
            }
        } catch (e: Exception) {
            Log.e("PreferencesAPI", "Exception: ${e.message}", e)
            false
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreferencesOnboardingPreview() {
    DamTheme {
        val navController = rememberNavController()
        PreferencesOnboardingScreen(navController = navController)
    }
}