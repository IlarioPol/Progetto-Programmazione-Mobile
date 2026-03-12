package com.example.progettoprogrammazionemobile.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthState
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState
    val user = (authState as? AuthState.Success)?.user

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.PasswordUpdated -> {
                showSuccessDialog = true
                viewModel.resetState()
            }
            is AuthState.AccountDeleted -> {
                onAccountDeleted()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Password Aggiornata") },
            text = { Text("La tua password è stata modificata con successo.") },
            confirmButton = {
                Button(onClick = { 
                    showSuccessDialog = false
                    onBack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina Account") },
            text = { Text("Sei sicuro di voler eliminare definitivamente il tuo account? Questa azione non può essere annullata.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Il Mio Profilo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Informazioni Utente
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dati Account", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nome: ${user?.name ?: "N/D"}")
                    Text("Email: ${user?.email ?: "N/D"}")
                    Text("Ruolo: ${user?.role?.name ?: "N/D"}")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sezione Modifica Password
            Text("Modifica Password", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nuova Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Conferma Nuova Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                        if (newPassword == confirmPassword) {
                            viewModel.updatePassword(newPassword)
                        } else {
                            // Errore locale se non coincidono
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salva Nuova Password")
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Pulsante Eliminazione Account
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elimina Account Definitivamente")
                }
            }
        }
    }
}
