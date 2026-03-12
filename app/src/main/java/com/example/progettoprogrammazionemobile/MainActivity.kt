package com.example.progettoprogrammazionemobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.progettoprogrammazionemobile.data.model.UserRole
import com.example.progettoprogrammazionemobile.ui.screens.auth.LoginScreen
import com.example.progettoprogrammazionemobile.ui.screens.auth.RegisterScreen
import com.example.progettoprogrammazionemobile.ui.screens.client.ClientHomeScreen
import com.example.progettoprogrammazionemobile.ui.screens.profile.ProfileScreen
import com.example.progettoprogrammazionemobile.ui.screens.provider.ProviderHomeScreen
import com.example.progettoprogrammazionemobile.ui.theme.ProgettoProgrammazioneMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProgettoProgrammazioneMobileTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { role ->
                                    val destination = if (role == UserRole.CLIENT) "client_home" else "provider_home"
                                    navController.navigate(destination) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onRegisterClick = { navController.navigate("register") }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { role ->
                                    val destination = if (role == UserRole.CLIENT) "client_home" else "provider_home"
                                    navController.navigate(destination) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onLoginClick = { navController.popBackStack() }
                            )
                        }
                        composable("client_home") {
                            ClientHomeScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }
                        composable("provider_home") {
                            ProviderHomeScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
