package com.example.dam

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dam.Screens.*
import com.example.dam.ui.theme.DamTheme
import com.example.dam.ui.theme.TabBarView
import com.example.dam.viewmodel.ForgotPasswordViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions


class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ AJOUT: Configuration Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // ton client_id
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            DamTheme {
                CycleApp(googleSignInClient = googleSignInClient, activity = this)
            }
        }
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

        composable(NavigationRoutes.ACTIVITY1) {
            ActivitiesSelectionScreen(navController = navController)
        }

        composable(NavigationRoutes.ACTIVITY2) {
            TerrainSelectionScreen(navController = navController)
        }

        composable(NavigationRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(NavigationRoutes.EDIT_PROFILE) {
            EditProfile1Screen(navController = navController)
        }

        composable(NavigationRoutes.EDIT_PROFILE1) {
            EditProfile2Screen(navController = navController)
        }

        composable(NavigationRoutes.HOME) {
            TabBarView(navController = navController)
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
    const val ACTIVITY1 = "ActivitiesSelectionScreen"
    const val ACTIVITY2 = "terrain_selection"
    const val EDIT_PROFILE = "edit_profile"
    const val EDIT_PROFILE1 = "editProfile1"
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    DamTheme {
        CycleApp() // pas besoin de passer googleSignInClient ni activity
    }
}
