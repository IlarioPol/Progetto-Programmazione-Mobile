package com.example.progettoprogrammazionemobile.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
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
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.ClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    clientViewModel: ClientViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedServiceForBooking by remember { mutableStateOf<Service?>(null) }
    var selectedServiceForReview by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Area Cliente") },
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
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Esplora") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Prenotazioni") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ExploreServicesTab(clientViewModel) { selectedServiceForBooking = it }
                1 -> MyBookingsTab(clientViewModel) { selectedServiceForReview = it }
            }
        }
    }

    selectedServiceForBooking?.let { service ->
        BookingDialog(
            serviceName = service.name,
            onDismiss = { selectedServiceForBooking = null },
            onConfirm = { date ->
                clientViewModel.bookService(service, date)
                selectedServiceForBooking = null
            }
        )
    }

    selectedServiceForReview?.let { serviceName ->
        ReviewDialog(
            serviceName = serviceName,
            onDismiss = { selectedServiceForReview = null },
            onConfirm = { rating, comment ->
                clientViewModel.addReview("id", serviceName, rating, comment)
                selectedServiceForReview = null
            }
        )
    }
}

@Composable
fun ExploreServicesTab(viewModel: ClientViewModel, onBookClick: (Service) -> Unit) {
    val services = viewModel.availableServices
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Servizi Disponibili", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onBookClick(service) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Prenota Ora")
                    }
                }
            }
        }
    }
}

@Composable
fun MyBookingsTab(viewModel: ClientViewModel, onReviewClick: (String) -> Unit) {
    val bookings = viewModel.myBookings
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Le Mie Prenotazioni", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (bookings.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Non hai ancora prenotazioni.", color = Color.Gray)
                }
            }
        }
        items(bookings) { booking ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(booking.serviceName, fontWeight = FontWeight.Bold)
                        Text(booking.status, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                    }
                    Text("Data: ${booking.date}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onReviewClick(booking.serviceName) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lascia Recensione")
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDialog(serviceName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prenota $serviceName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Inserisci la data e l'ora preferita:")
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Data (GG/MM/AAAA)") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Ora (HH:MM)") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm("$date $time") }) { Text("Conferma") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}

@Composable
fun ReviewDialog(serviceName: String, onDismiss: () -> Unit, onConfirm: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recensisci $serviceName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Valutazione:")
                Row {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < rating) Color(0xFFFFB400) else Color.Gray
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commento") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(rating, comment) }) { Text("Invia") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
