package com.example.progettoprogrammazionemobile.ui.screens.provider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.DayAvailability
import com.example.progettoprogrammazionemobile.data.model.TimeRange
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel
import com.example.progettoprogrammazionemobile.ui.viewmodel.ProviderViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderHomeScreen(
    onLogout: () -> Unit,
    onProfileClick: () -> Unit,
    providerViewModel: ProviderViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<Service?>(null) }
    
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
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Prenotazioni") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Recensioni") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Orari") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Stats") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ServicesListTab(
                    viewModel = providerViewModel,
                    onEditClick = { serviceToEdit = it },
                    onDeleteClick = { providerViewModel.deleteService(it) }
                )
                1 -> ProviderBookingsTab(providerViewModel)
                2 -> ReviewsListTab(providerViewModel)
                3 -> AvailabilityTab(providerViewModel)
                4 -> StatisticsTab(providerViewModel)
            }
        }
    }

    if (pendingInviteManagerId != null) {
        AlertDialog(
            onDismissRequest = { },
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
        ServiceDialog(
            title = "Nuovo Servizio",
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, dur, price ->
                providerViewModel.addService(name, desc, dur, price)
                showAddDialog = false
            }
        )
    }

    serviceToEdit?.let { service ->
        ServiceDialog(
            title = "Modifica Servizio",
            initialName = service.name,
            initialDesc = service.description,
            initialDuration = service.durationMinutes.toString(),
            initialPrice = service.price.toString(),
            onDismiss = { serviceToEdit = null },
            onConfirm = { name, desc, dur, price ->
                providerViewModel.updateService(service.id, name, desc, dur, price)
                serviceToEdit = null
            }
        )
    }
}

@Composable
fun ServicesListTab(
    viewModel: ProviderViewModel,
    onEditClick: (Service) -> Unit,
    onDeleteClick: (String) -> Unit
) {
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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { onEditClick(service) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onDeleteClick(service.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProviderBookingsTab(viewModel: ProviderViewModel) {
    val bookings = viewModel.incomingBookings
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Richieste Prenotazione", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (bookings.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessuna richiesta in attesa.", color = Color.Gray)
                }
            }
        }
        items(bookings) { booking ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when(booking.status) {
                        "Pending" -> MaterialTheme.colorScheme.surfaceVariant
                        "Confirmed" -> Color(0xFFE8F5E9)
                        "Rejected" -> Color(0xFFFFEBEE)
                        "Completed" -> Color(0xFFE3F2FD)
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(booking.serviceName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Badge(
                            containerColor = when(booking.status) {
                                "Confirmed" -> Color(0xFF4CAF50)
                                "Completed" -> Color(0xFF2196F3)
                                "Rejected" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ) {
                            Text(booking.status, color = Color.White)
                        }
                    }
                    Text("Data: ${booking.date}", style = MaterialTheme.typography.bodyMedium)
                    
                    when (booking.status) {
                        "Pending" -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.updateBookingStatus(booking.id, "Rejected") },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rifiuta")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.updateBookingStatus(booking.id, "Confirmed") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Accetta")
                                }
                            }
                        }
                        "Confirmed" -> {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.updateBookingStatus(booking.id, "Completed") },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Segna come Completato")
                            }
                        }
                    }
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
fun AvailabilityTab(viewModel: ProviderViewModel) {
    val availability by viewModel.availability
    val days = listOf("Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì", "Sabato", "Domenica")

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text("Gestione Orari di Lavoro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Definisci quando sei disponibile per le prenotazioni", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        availability?.let { avail ->
            items(avail.weeklyAvailability, key = { it.dayOfWeek }) { dayAvail ->
                DayAvailabilityItem(
                    dayName = days[dayAvail.dayOfWeek - 1],
                    dayAvailability = dayAvail,
                    onUpdate = { updatedDay ->
                        val newList = avail.weeklyAvailability.map {
                            if (it.dayOfWeek == updatedDay.dayOfWeek) updatedDay else it
                        }
                        viewModel.updateAvailability(avail.copy(weeklyAvailability = newList))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayAvailabilityItem(
    dayName: String,
    dayAvailability: DayAvailability,
    onUpdate: (DayAvailability) -> Unit
) {
    var showTimePickerForRangeIndex by remember { mutableStateOf<Int?>(null) }
    var isStartTimePicker by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (dayAvailability.workDay) MaterialTheme.colorScheme.surface else Color(0xFFF5F5F5)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Switch(
                    checked = dayAvailability.workDay,
                    onCheckedChange = { onUpdate(dayAvailability.copy(workDay = it)) }
                )
            }

            if (dayAvailability.workDay) {
                dayAvailability.timeRanges.forEachIndexed { index, range ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        AssistChip(
                            onClick = { 
                                showTimePickerForRangeIndex = index
                                isStartTimePicker = true
                            },
                            label = { Text(range.startTime) }
                        )
                        Text(" - ", modifier = Modifier.padding(horizontal = 8.dp))
                        AssistChip(
                            onClick = { 
                                showTimePickerForRangeIndex = index
                                isStartTimePicker = false
                            },
                            label = { Text(range.endTime) }
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (dayAvailability.timeRanges.size > 1) {
                            IconButton(onClick = {
                                val newList = dayAvailability.timeRanges.toMutableList()
                                newList.removeAt(index)
                                onUpdate(dayAvailability.copy(timeRanges = newList))
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Rimuovi")
                            }
                        }
                    }
                }
                
                TextButton(
                    onClick = {
                        val newList = dayAvailability.timeRanges.toMutableList()
                        newList.add(TimeRange("09:00", "18:00"))
                        onUpdate(dayAvailability.copy(timeRanges = newList))
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Aggiungi Fascia Oraria")
                }
            } else {
                Text("Chiuso", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showTimePickerForRangeIndex != null) {
        val rangeIndex = showTimePickerForRangeIndex!!
        val currentRange = dayAvailability.timeRanges[rangeIndex]
        val currentTime = if (isStartTimePicker) currentRange.startTime else currentRange.endTime
        val parts = currentTime.split(":")
        val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
        val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )

        Dialog(onDismissRequest = { showTimePickerForRangeIndex = null }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isStartTimePicker) "Seleziona inizio" else "Seleziona fine",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTimePickerForRangeIndex = null }) {
                            Text("Annulla")
                        }
                        TextButton(onClick = {
                            val newTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                            val newList = dayAvailability.timeRanges.toMutableList()
                            val updatedRange = if (isStartTimePicker) {
                                currentRange.copy(startTime = newTime)
                            } else {
                                currentRange.copy(endTime = newTime)
                            }
                            newList[rangeIndex] = updatedRange
                            onUpdate(dayAvailability.copy(timeRanges = newList))
                            showTimePickerForRangeIndex = null
                        }) {
                            Text("Conferma")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsTab(viewModel: ProviderViewModel) {
    val turnover by viewModel.totalTurnover
    val servicesCount = viewModel.services.size
    val completedCount = viewModel.incomingBookings.count { it.status == "Completed" }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Le Tue Statistiche", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(), 
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fatturato Totale", style = MaterialTheme.typography.titleMedium)
                Text("€${String.format(Locale.getDefault(), "%.2f", turnover)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Servizi Attivi", style = MaterialTheme.typography.bodySmall)
                    Text("$servicesCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Card(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lavori Fatti", style = MaterialTheme.typography.bodySmall)
                    Text("$completedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ServiceDialog(
    title: String,
    initialName: String = "",
    initialDesc: String = "",
    initialDuration: String = "",
    initialPrice: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDesc) }
    var duration by remember { mutableStateOf(initialDuration) }
    var price by remember { mutableStateOf(initialPrice) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
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
            }) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
