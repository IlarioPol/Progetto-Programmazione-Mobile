package com.example.progettoprogrammazionemobile.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
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
fun RegisterScreen(
    onRegisterSuccess: (UserRole) -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CLIENT) }
    var showVerificationMessage by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                onRegisterSuccess(state.user.role)
                viewModel.resetState()
            }
            is AuthState.VerificationEmailSent -> {
                showVerificationMessage = true
            }
            else -> {}
        }
    }

    if (showVerificationMessage) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss by clicking outside */ },
            title = { Text("Verifica Email") },
            text = { Text("Ti abbiamo inviato un'email di verifica a $email. Per favore controlla la tua posta e clicca sul link prima di accedere.") },
            confirmButton = {
                Button(onClick = {
                    showVerificationMessage = false
                    viewModel.resetState()
                    onLoginClick()
                }) {
                    Text("Vai al Login")
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
        Text(text = "Registrazione", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Conferma Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("Iscriviti come:", style = MaterialTheme.typography.titleMedium)
        Column(Modifier.selectableGroup()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == UserRole.CLIENT,
                    onClick = { selectedRole = UserRole.CLIENT }
                )
                Text("Cliente", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == UserRole.PROVIDER,
                    onClick = { selectedRole = UserRole.PROVIDER }
                )
                Text("Emettitore Servizi (Provider)", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == UserRole.MANAGER,
                    onClick = { selectedRole = UserRole.MANAGER }
                )
                Text("Responsabile (Manager)", modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val state = authState
        if (state is AuthState.Error) {
            Text(
                text = state.message,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (state is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.register(name, email, password, confirmPassword, selectedRole) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrati")
            }
        }

        TextButton(onClick = onLoginClick) {
            Text("Hai già un account? Accedi")
        }
    }
}
