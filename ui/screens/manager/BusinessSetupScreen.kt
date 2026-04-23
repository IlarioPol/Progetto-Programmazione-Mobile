package com.example.progettoprogrammazionemobile.ui.screens.manager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthState
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var businessName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Medicina") }
    
    val categories = listOf("Medicina", "Legal", "Beauty", "Ristorazione", "Istruzione", "Altro")
    var expanded by remember { mutableStateOf(false) }

    val authState by viewModel.authState

    LaunchedEffect(authState) {
        if (authState is AuthState.BusinessCreated) {
            onSetupComplete()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Configura la tua Azienda", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Completa i dati per iniziare a gestire il tuo team", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Nome Azienda / Studio") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Macro-Categoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Indirizzo Sede") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Breve Descrizione") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.Error) {
            Text(text = (authState as AuthState.Error).message, color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { 
                    viewModel.createBusiness(businessName, selectedCategory, description, address) 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = businessName.isNotBlank() && address.isNotBlank()
            ) {
                Text("Salva e Continua")
            }
        }
    }
}
