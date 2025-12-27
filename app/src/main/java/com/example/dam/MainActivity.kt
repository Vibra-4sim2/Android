package com.example.dam

import android.app.Activity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.dam.Screens.ProfileScreen
import com.example.dam.Screens.SplashScreen
import com.example.dam.Screens.RegisterScreen
import com.example.dam.Screens.PreferencesOnboardingScreen
import com.example.dam.Screens.EditProfile2Screen
import com.example.dam.Screens.EditProfile1Screen
import com.example.dam.Screens.ForgotPasswordScreen
import com.example.dam.Screens.ResetPasswordScreen
import com.example.dam.Screens.OnboardingScreen
import com.example.dam.Screens.SortieDetailScreen



import com.example.dam.Screens.*
import com.example.dam.Screens.FeedScreen
import com.example.dam.ui.theme.DamTheme
import com.example.dam.ui.theme.TabBarView
import com.example.dam.viewmodel.ForgotPasswordViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.MapsInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ✅ IMPORTS POUR LES NOTIFICATIONS
import com.example.dam.services.NotificationPollingService
import com.example.dam.utils.NotificationHelper
import com.example.dam.utils.UserPreferences
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    // ✅ Launcher pour demander la permission de notification (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "✅ Notification permission granted")
        } else {
            Log.w("MainActivity", "⚠️ Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // ✅ SOLUTION: Utiliser penaltyDeath() ou simplement ne rien faire
        // Option 1: Politique permissive (ne bloque rien)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork()
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .build()  // ✅ Politique vide = pas de restrictions
        )



        // ✅ Pré-initialiser Google Maps
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { }
            } catch (e: Exception) {
                Log.e("MainActivity", "Maps init error: ${e.message}")
            }
        }
        // ✅ AJOUT: Configuration Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ton client_id
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ✅ INITIALISER LE SYSTÈME DE NOTIFICATION
        initializeNotificationSystem()

        // ✅ GÉRER LES DEEP LINKS (notifications tapées)
        handleNotificationIntent(intent)

        setContent {
            DamTheme {
                CycleApp(googleSignInClient = googleSignInClient, activity = this)
            }
        }
    }

    // ✅ Gérer les nouveaux intents (quand l'app est déjà ouverte)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleNotificationIntent(it) }
    }

    /**
     * Initialise le système de notification
     */
    private fun initializeNotificationSystem() {
        // Créer le canal de notification
        NotificationHelper.createNotificationChannel(this)

        // Demander la permission pour Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Démarrer le polling si l'utilisateur est connecté
        val token = UserPreferences.getToken(this)
        if (!token.isNullOrEmpty()) {
            Log.d("MainActivity", "🚀 User logged in, starting notification polling")
            NotificationPollingService.startPolling(this, intervalSeconds = 15)
        } else {
            Log.d("MainActivity", "⚠️ No token found, polling will start after login")
        }
    }

    /**
     * Gère les deep links depuis les notifications
     */
    private fun handleNotificationIntent(intent: Intent) {
        val notificationType = intent.getStringExtra("notification_type") ?: return
        val notificationId = intent.getStringExtra("notification_id")

        Log.d("MainActivity", "📲 Deep link detected: $notificationType")

        // ✅ Mark notification as read when clicked from system notification
        if (!notificationId.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val token = UserPreferences.getToken(this@MainActivity)
                    if (!token.isNullOrEmpty()) {
                        val repository = com.example.dam.repository.NotificationRepository()
                        repository.markAsRead(token, notificationId)
                        Log.d("MainActivity", "✅ Notification marked as read: $notificationId")
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "❌ Failed to mark notification as read", e)
                }
            }
        }

        // TODO: Navigator vers l'écran approprié selon le type
        // Cette logique sera implémentée dans le NavController
        when (notificationType) {
            "NEW_PUBLICATION" -> {
                val publicationId = intent.getStringExtra("publicationId")
                Log.d("MainActivity", "→ Navigate to feed (publication list): $publicationId")
                // Redirection vers l'écran feed car pas d'écran détail publication
                // TODO: Implémenter la navigation vers feed quand NavController sera disponible
            }
            "CHAT_MESSAGE" -> {
                val sortieId = intent.getStringExtra("sortieId")
                Log.d("MainActivity", "→ Navigate to chat: $sortieId")
                // Exemple: navController.navigate("chatConversation/$sortieId")
            }
            "NEW_SORTIE" -> {
                val sortieId = intent.getStringExtra("sortieId")
                Log.d("MainActivity", "→ Navigate to sortie: $sortieId")
                // Exemple: navController.navigate("sortieDetail/$sortieId")
            }
            "PARTICIPATION_ACCEPTED", "PARTICIPATION_REJECTED" -> {
                val sortieId = intent.getStringExtra("sortieId")
                Log.d("MainActivity", "→ Navigate to sortie: $sortieId")
                // Exemple: navController.navigate("sortieDetail/$sortieId")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Arrêter le polling si l'app est fermée
        NotificationPollingService.stopPolling()
    }
}

@Composable
fun CycleApp(
    googleSignInClient: GoogleSignInClient? = null,
    activity: Activity? = null
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavigationGraph(
            navController = navController,
            googleSignInClient = googleSignInClient,
            activity = activity
        )
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    googleSignInClient: GoogleSignInClient?,
    activity: Activity?
) {
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.SPLASH
    ) {
        composable(NavigationRoutes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(NavigationRoutes.LOGIN) {
            if (googleSignInClient != null && activity != null) {
                LoginScreen(
                    navController = navController,
                    googleSignInClient = googleSignInClient,
                    activity = activity
                )
            }
        }

        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        composable(NavigationRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                navController = navController,
                viewModel = forgotPasswordViewModel
            )
        }

        composable(NavigationRoutes.RESET_PASSWORD) {
            ResetPasswordScreen(
                navController = navController,
                viewModel = forgotPasswordViewModel
            )
        }

        composable(NavigationRoutes.ONBOARDING1) {
            OnboardingScreen(navController = navController)
        }

        composable(NavigationRoutes.PREFERENCES) {
            PreferencesOnboardingScreen(navController = navController)
        }


        composable(NavigationRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }


        // User Profile Screen Route
        composable(
            "userProfile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserProfileScreen(
                navController = navController,
                userId = it.arguments?.getString("userId")!!
            )
        }



        composable(
            route = "participation_requests/{sortieId}",
            arguments = listOf(
                navArgument("sortieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
            ParticipationRequestsScreen(
                navController = navController,
                sortieId = sortieId
            )
        }


        composable(NavigationRoutes.EDIT_PROFILE) {
            EditProfile1Screen(navController = navController)
        }

        composable(NavigationRoutes.EDIT_PROFILE1) {
            EditProfile2Screen(navController = navController)
        }

        composable(NavigationRoutes.FEED) {
            FeedScreen(navController = navController)
        }

        composable(NavigationRoutes.ADD_PUB) {
            AddPublicationScreen(navController = navController)
        }


        composable(NavigationRoutes.HOME) {
            TabBarView(navController = navController)
        }

        composable(NavigationRoutes.CREATE) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token") ?: ""
            CreateAdventureScreen(
                navController = navController,
                token = token
            )
        }




        // ✅ AJOUTE CETTE ROUTE ICI (après sortieDetail ou où tu veux)
        composable(
            route = "participation_requests/{sortieId}",
            arguments = listOf(
                navArgument("sortieId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
            ParticipationRequestsScreen(
                navController = navController,
                sortieId = sortieId
            )
        }


        // ✅ NEW: Sortie Detail Screen with ID parameter
        composable(
            route = "sortieDetail/{sortieId}",
            arguments = listOf(
                navArgument("sortieId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
            SortieDetailScreen(
                navController = navController,
                sortieId = sortieId
            )
        }

        composable(NavigationRoutes.MESSAGES) {
            MessagesListScreen(navController = navController)
        }

        // ✅ CORRIGÉ: Route pour la conversation de chat
        composable(
            route = "chatConversation/{sortieId}/{groupName}/{groupEmoji}/{participantsCount}",
            arguments = listOf(
                navArgument("sortieId") { type = NavType.StringType },
                navArgument("groupName") { type = NavType.StringType },
                navArgument("groupEmoji") { type = NavType.StringType },
                navArgument("participantsCount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sortieId = backStackEntry.arguments?.getString("sortieId") ?: ""
            val encodedGroupName = backStackEntry.arguments?.getString("groupName") ?: ""
            val encodedEmoji = backStackEntry.arguments?.getString("groupEmoji") ?: ""
            val participantsCount = backStackEntry.arguments?.getString("participantsCount") ?: ""

            // Décoder les paramètres qui contiennent des caractères spéciaux
            val groupName = java.net.URLDecoder.decode(encodedGroupName, "UTF-8")
            val groupEmoji = java.net.URLDecoder.decode(encodedEmoji, "UTF-8")

            ChatConversationScreen(
                navController = navController,
                sortieId = sortieId, // ✅ CORRIGÉ: utiliser sortieId au lieu de groupId
                groupName = groupName,
                groupEmoji = groupEmoji,
                participantsCount = participantsCount
            )
        }

        // ✅ AJOUTEZ CES 4 NOUVELLES ROUTES
        composable(NavigationRoutes.RECOMMENDATION_HUB) {
            RecommendationHubScreen(navController = navController)
        }

        // ✅ NEW: Flask AI Recommendations
        composable(NavigationRoutes.FLASK_RECOMMENDATIONS) {
            FlaskAiRecommendationsScreen(navController = navController)
        }

        // ✅ NEW: Flask Matchmaking
        composable(NavigationRoutes.FLASK_MATCHMAKING) {
            FlaskMatchmakingScreen(navController = navController)
        }

        composable("flask_itinerary") {
            FlaskItineraryScreen(navController = navController)
        }




    }
}
// ✅ Routes mises à jour
object NavigationRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val RESET_PASSWORD = "reset_password"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val SPLASH = "splash"
    const val ONBOARDING1 = "onboarding1"
    const val PREFERENCES = "preferences"
    const val EDIT_PROFILE = "edit_profile"
    const val EDIT_PROFILE1 = "editProfile1"
    const val FEED = "feed"
    const val ADD_PUB = "addpublication"
    const val SORTIE_DETAIL = "sortieDetail/{sortieId}"
    // ✅ AJOUTEZ CES 4 LIGNES
    const val RECOMMENDATION_HUB = "recommendation_hub"
    const val PREFERENCE_RECOMMENDATIONS = "preference_recommendations"
    const val WEATHER_RECOMMENDATIONS = "weather_recommendations"
    const val SMART_MATCHES = "smart_matches"

    // ✅ NEW: Flask AI Routes
    const val FLASK_RECOMMENDATIONS = "flask_recommendations"
    const val FLASK_MATCHMAKING = "flask_matchmaking"

    // ← NEW: Accept token
    const val CREATE = "createadventure/{token}"
    const val USER_PROFILE = "userProfile/{userId}"
    const val MESSAGES = "messages"

    // ✅ Route pour les notifications
    const val NOTIFICATIONS = "notifications"

    // Helper functions
    fun createAdventureRoute(token: String) = "createadventure/$token"
    fun userProfileRoute(userId: String) = "userProfile/$userId"
    fun sortieDetailRoute(sortieId: String) = "sortieDetail/$sortieId"

    // ✅ CORRIGÉ: Helper function avec encodage URL
    fun chatConversationRoute(
        sortieId: String,
        groupName: String,
        groupEmoji: String,
        participantsCount: String
    ): String {
        val encodedGroupName = java.net.URLEncoder.encode(groupName, "UTF-8")
        val encodedEmoji = java.net.URLEncoder.encode(groupEmoji, "UTF-8")
        return "chatConversation/$sortieId/$encodedGroupName/$encodedEmoji/$participantsCount"
    }


    fun participationRequestsRoute(sortieId: String) = "participation_requests/$sortieId"

}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    DamTheme {
        CycleApp()
    }
}
