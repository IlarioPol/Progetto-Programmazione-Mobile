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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.progettoprogrammazionemobile.data.model.UserRole
import com.example.progettoprogrammazionemobile.ui.screens.auth.LoginScreen
import com.example.progettoprogrammazionemobile.ui.screens.auth.RegisterScreen
import com.example.progettoprogrammazionemobile.ui.screens.client.ClientHomeScreen
import com.example.progettoprogrammazionemobile.ui.screens.client.BusinessDetailScreen
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
                
                // Osserva la rotta corrente per la logica di redirezione
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Gestione centralizzata della navigazione basata sull'autenticazione
                LaunchedEffect(authState, currentRoute) {
                    when (authState) {
                        is AuthState.Success -> {
                            val user = (authState as AuthState.Success).user
                            // Se l'utente è loggato ma si trova su Login o Register, lo mandiamo alla Home corretta
                            if (currentRoute == "login" || currentRoute == "register") {
                                val destination = when (user.role) {
                                    UserRole.CLIENT -> "client_home"
                                    UserRole.PROVIDER -> "provider_home"
                                    UserRole.MANAGER -> {
                                        if (user.businessId == null) "business_setup"
                                        else "manager_home"
                                    }
                                }
                                navController.navigate(destination) {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                        is AuthState.Idle -> {
                            // Se non siamo loggati e non siamo già su Login o Register, forziamo il ritorno al login
                            if (currentRoute != "login" && currentRoute != "register" && currentRoute != null) {
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        else -> {}
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
                                onLoginSuccess = { /* Gestito da LaunchedEffect */ },
                                onRegisterClick = { navController.navigate("register") },
                                viewModel = authViewModel
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { /* Gestito da LaunchedEffect */ },
                                onLoginClick = { navController.popBackStack() },
                                viewModel = authViewModel
                            )
                        }
                        composable("business_setup") {
                            BusinessSetupScreen(
                                onSetupComplete = {
                                    // Dopo il setup, forziamo un refresh dei dati utente per aggiornare il businessId
                                    authViewModel.checkCurrentUser()
                                },
                                viewModel = authViewModel
                            )
                        }
                        composable("client_home") {
                            ClientHomeScreen(
                                onLogout = { authViewModel.logout() },
                                onProfileClick = { navController.navigate("profile") },
                                onBusinessClick = { businessId ->
                                    navController.navigate("business_detail/$businessId")
                                },
                                authViewModel = authViewModel
                            )
                        }
                        composable("business_detail/{businessId}") { backStackEntry ->
                            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
                            BusinessDetailScreen(
                                businessId = businessId,
                                onBack = { navController.popBackStack() },
                                onBookService = { /* Gestito internamente */ }
                            )
                        }
                        composable("provider_home") {
                            ProviderHomeScreen(
                                onLogout = { authViewModel.logout() },
                                onProfileClick = { navController.navigate("profile") },
                                authViewModel = authViewModel
                            )
                        }
                        composable("manager_home") {
                            ManagerHomeScreen(
                                onLogout = { authViewModel.logout() },
                                onProfileClick = { navController.navigate("profile") },
                                authViewModel = authViewModel
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() },
                                onAccountDeleted = { authViewModel.logout() },
                                viewModel = authViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
