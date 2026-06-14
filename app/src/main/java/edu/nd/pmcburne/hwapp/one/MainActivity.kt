package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.nd.pmcburne.hwapp.one.ui.screens.ChatScreen
import edu.nd.pmcburne.hwapp.one.ui.screens.CreateGameScreen
import edu.nd.pmcburne.hwapp.one.ui.screens.GameDetailScreen
import edu.nd.pmcburne.hwapp.one.ui.screens.HomeScreen
import edu.nd.pmcburne.hwapp.one.ui.screens.LoginScreen
import edu.nd.pmcburne.hwapp.one.ui.screens.ProfileScreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGameTheme
import edu.nd.pmcburne.hwapp.one.viewmodel.AuthViewModel
import edu.nd.pmcburne.hwapp.one.viewmodel.LocationViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PickupGameTheme {
                val authState by authViewModel.authState.collectAsState()
                val navController = rememberNavController()

                val startDestination = if (authState.isLoggedIn) "home" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            locationViewModel = locationViewModel,
                            onCreateGame = { navController.navigate("create") },
                            onProfile = { navController.navigate("profile") },
                            onOpenGame = { id -> navController.navigate("game/$id") }
                        )
                    }

                    composable("create") {
                        CreateGameScreen(
                            locationViewModel = locationViewModel,
                            onBack = { navController.popBackStack() },
                            onPosted = { navController.popBackStack() }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            authViewModel = authViewModel,
                            onHome = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onOpenGame = { id -> navController.navigate("game/$id") },
                            onSignedOut = {
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("game/{id}") { backStack ->
                        val id = backStack.arguments?.getString("id") ?: ""
                        GameDetailScreen(
                            gameId = id,
                            onBack = { navController.popBackStack() },
                            onChat = { gid -> navController.navigate("chat/$gid") }
                        )
                    }

                    composable("chat/{id}") { backStack ->
                        val id = backStack.arguments?.getString("id") ?: ""
                        ChatScreen(
                            gameId = id,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
