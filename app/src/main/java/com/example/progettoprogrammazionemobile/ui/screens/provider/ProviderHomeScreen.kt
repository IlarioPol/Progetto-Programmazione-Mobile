package com.example.progettoprogrammazionemobile.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.ProviderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    providerViewModel: ProviderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    val pendingInviteManagerId by authViewModel.pendingInvitation

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard Emettitore") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profilo")
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi Servizio")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Servizi") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Recensioni") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Stats") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ServicesListTab(providerViewModel)
                1 -> ReviewsListTab(providerViewModel)
                2 -> StatisticsTab(providerViewModel)
            }
        }
    }

    // Dialog per invito pendente
    pendingInviteManagerId?.let { managerId ->
        AlertDialog(
            onDismissRequest = { /* Obbliga a scegliere */ },
            title = { Text("Invito Supervisione") },
            text = { Text("Un Responsabile desidera supervisionare il tuo account. Accettando, potrà visualizzare le tue statistiche e i tuoi servizi.") },
            confirmButton = {
                Button(onClick = { authViewModel.acceptInvitation() }) {
                    Text("Accetta")
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.declineInvitation() }) {
                    Text("Rifiuta")
                }
            }
        )
    }

    if (showAddDialog) {
        AddServiceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, dur, price ->
                providerViewModel.addService(name, desc, dur, price)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ServicesListTab(viewModel: ProviderViewModel) {
    val services = viewModel.services
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("I Tuoi Servizi", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(services) { service ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("€${service.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Text(service.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Durata: ${service.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ReviewsListTab(viewModel: ProviderViewModel) {
    val reviews = viewModel.reviews
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Recensioni Clienti", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(reviews) { review ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(review.clientName, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        repeat(review.rating) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(review.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(review.comment)
                }
            }
        }
    }
}

@Composable
fun StatisticsTab(viewModel: ProviderViewModel) {
    val turnover by viewModel.totalTurnover
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Statistiche Generali", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fatturato Totale", style = MaterialTheme.typography.titleMedium)
                Text("€$turnover", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Servizi Attivi", style = MaterialTheme.typography.bodySmall)
                    Text("${viewModel.services.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Media Voti", style = MaterialTheme.typography.bodySmall)
                    Text("4.5", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddServiceDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuovo Servizio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descrizione") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Durata (min)") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Prezzo (€)") })
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(name, desc, duration.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0)
            }) { Text("Aggiungi") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
