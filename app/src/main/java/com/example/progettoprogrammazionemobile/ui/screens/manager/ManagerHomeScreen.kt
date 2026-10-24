package com.example.progettoprogrammazionemobile.ui.screens.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.ManagerViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerHomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    managerViewModel: ManagerViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val business = authViewModel.userBusiness.value

    LaunchedEffect(business) {
        managerViewModel.fetchMyProviders(business?.id)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(business?.name ?: "Area Manager") },
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
                FloatingActionButton(onClick = { showInviteDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Invita Provider")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Team") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Statistiche") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            when (selectedTab) {
                0 -> TeamTab(managerViewModel, business)
                1 -> StatsTab(managerViewModel)
            }
        }
    }

    if (showInviteDialog) {
        InviteProviderDialog(
            onDismiss = { showInviteDialog = false },
            onConfirm = { email ->
                managerViewModel.inviteProvider(email, business?.id, business?.name)
                showInviteDialog = false
            }
        )
    }
}

@Composable
fun TeamTab(viewModel: ManagerViewModel, business: com.example.progettoprogrammazionemobile.data.model.Business?) {
    Text("Professionisti del Team", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    if (business != null) {
        Text(text = "Categoria: ${business.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
    Spacer(modifier = Modifier.height(16.dp))
    
    if (viewModel.myProviders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessun professionista associato.", color = Color.Gray)
        }
    } else {
        LazyColumn {
            items(viewModel.myProviders) { provider ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    ListItem(
                        headlineContent = { Text(provider.name) },
                        supportingContent = { Text(provider.email) },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsTab(viewModel: ManagerViewModel) {
    val locale = LocalConfiguration.current.locales[0]
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Dashboard Analitica", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }

        // Turnover Cards
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Fatturato Totale", "€${String.format(locale, "%.2f", viewModel.totalTurnover.value)}", Modifier.weight(1f))
                StatCard("Questo Mese", "€${String.format(locale, "%.2f", viewModel.monthlyTurnover.value)}", Modifier.weight(1f))
            }
        }

        // Rating
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rating Medio Team", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(locale, "%.1f", viewModel.averageRating.value),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(40.dp))
                    }
                }
            }
        }

        // Provider Performance
        item {
            Text("Performance Professionisti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(viewModel.providerStats) { stat ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(stat.name, fontWeight = FontWeight.Bold)
                        Text("Rating: ${String.format(locale, "%.1f", stat.rating)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("€${String.format(locale, "%.2f", stat.turnover)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Service Trends
        item {
            Text("Servizi più richiesti", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(viewModel.serviceTrends) { trend ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(trend.name)
                    Text("${trend.count} prenotazioni", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun InviteProviderDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invita nuovo Provider") },
        text = {
            Column {
                Text("Inserisci l'email del professionista da aggiungere alla tua azienda:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(email) }) { Text("Invia Invito") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Annulla")
            }
        }
    )
}
