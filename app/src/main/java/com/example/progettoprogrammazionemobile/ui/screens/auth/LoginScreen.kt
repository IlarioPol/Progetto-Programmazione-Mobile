package com.example.progettoprogrammazionemobile.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.data.model.UserRole
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthState
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (UserRole) -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val authState by viewModel.authState

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onLoginSuccess((authState as AuthState.Success).user.role)
                viewModel.resetState()
            }
            is AuthState.PasswordResetSent -> {
                successMessage = "Email di reset inviata! Controlla la tua posta."
                showResetDialog = false
                viewModel.resetState()
            }
            else -> {}
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Recupero Password") },
            text = {
                Column {
                    Text("Inserisci la tua email per ricevere il link di reset:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.resetPassword(resetEmail) }) {
                    Text("Invia")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Prenotazione Servizi", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Accedi al tuo account", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { 
                resetEmail = email
                showResetDialog = true 
            }) {
                Text("Password dimenticata?")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (successMessage != null) {
            Text(
                text = successMessage!!,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Reset success message after some time or interaction
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    successMessage = null
                    viewModel.login(email, password) 
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accedi")
            }
        }
        
        TextButton(onClick = onRegisterClick) {
            Text("Non hai un account? Registrati")
        }
    }
}
