package com.example.progettoprogrammazionemobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.progettoprogrammazionemobile.data.model.UserRole
import com.example.progettoprogrammazionemobile.ui.screens.auth.LoginScreen
import com.example.progettoprogrammazionemobile.ui.screens.auth.RegisterScreen
import com.example.progettoprogrammazionemobile.ui.screens.client.ClientHomeScreen
import com.example.progettoprogrammazionemobile.ui.screens.manager.BusinessSetupScreen
import com.example.progettoprogrammazionemobile.ui.screens.manager.ManagerHomeScreen
import com.example.progettoprogrammazionemobile.ui.screens.profile.ProfileScreen
import com.example.progettoprogrammazionemobile.ui.screens.provider.ProviderHomeScreen
import com.example.progettoprogrammazionemobile.ui.theme.ProgettoProgrammazioneMobileTheme
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthState
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProgettoProgrammazioneMobileTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.authState
                
                // Effettua il controllo automatico del business quando l'utente è loggato
                LaunchedEffect(authState) {
                    if (authState is AuthState.Success) {
                        val user = (authState as AuthState.Success).user
                        if (user.role == UserRole.MANAGER && user.businessId != null) {
                            // Se siamo nella schermata di login o setup e abbiamo già un businessId, vai alla home
                            val currentRoute = navController.currentDestination?.route
                            if (currentRoute == "login" || currentRoute == "business_setup") {
                                navController.navigate("manager_home") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = { role ->
                                    val user = (authViewModel.authState.value as? AuthState.Success)?.user
                                    val destination = when (role) {
                                        UserRole.CLIENT -> "client_home"
                                        UserRole.PROVIDER -> "provider_home"
                                        UserRole.MANAGER -> {
                                            if (user?.businessId == null) "business_setup"
                                            else "manager_home"
                                        }
                                    }
                                    navController.navigate(destination) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onRegisterClick = { navController.navigate("register") },
                                viewModel = authViewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { role ->
                                    val destination = when (role) {
                                        UserRole.CLIENT -> "client_home"
                                        UserRole.PROVIDER -> "provider_home"
                                        UserRole.MANAGER -> "business_setup"
                                    }
                                    navController.navigate(destination) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onLoginClick = { navController.popBackStack() },
                                viewModel = authViewModel
                            )
                        }
                        composable("business_setup") {
                            BusinessSetupScreen(
                                onSetupComplete = {
                                    navController.navigate("manager_home") {
                                        popUpTo("business_setup") { inclusive = true }
                                    }
                                },
                                viewModel = authViewModel
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
                        composable("manager_home") {
                            ManagerHomeScreen(
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onAccountDeleted = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                viewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
