package com.example.dam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dam.Screens.ForgotPasswordScreen
import com.example.dam.Screens.LoginScreen
import com.example.dam.Screens.OnboardingScreen
import com.example.dam.Screens.RegisterScreen
import com.example.dam.Screens.SplashScreen
import com.example.dam.ui.theme.DamTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DamTheme {
                CycleApp()
            }
        }
    }
}

@Composable
fun CycleApp() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavigationGraph(navController = navController)
    }
}

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.SPLASH
        // Removed problematic transition overrides
    ) {
        composable(NavigationRoutes.SPLASH) {
            SplashScreen(navController = navController)
        }

        composable(NavigationRoutes.LOGIN) {
            LoginScreen(navController = navController)
        }

        composable(NavigationRoutes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        composable(NavigationRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(NavigationRoutes.ONBOARDING1) {
            OnboardingScreen(navController = navController)
        }

        composable(NavigationRoutes.HOME) {
            // Simple home screen for testing
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Welcome Home!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Button(
                        onClick = {
                            navController.navigate(NavigationRoutes.LOGIN) {
                                popUpTo(NavigationRoutes.HOME) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}

// Routes de navigation centralisées
object NavigationRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val SPLASH = "splash"
    const val ONBOARDING1 = "onboarding1"

}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    DamTheme {
        CycleApp()
    }
}