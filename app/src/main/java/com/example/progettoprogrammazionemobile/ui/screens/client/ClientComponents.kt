package com.example.progettoprogrammazionemobile.ui.screens.client

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.ui.viewmodel.ClientViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    service: Service,
    viewModel: ClientViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val availability by viewModel.selectedProviderAvailability
    val locale = LocalConfiguration.current.locales[0]
    
    // Configura i giorni selezionabili e disabilita il passato
    val selectableDates = remember(availability) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = utcTimeMillis
                
                // Disabilita date passate
                val today = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (calendar.before(today)) return false

                if (availability == null) return true
                
                val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> 1
                    Calendar.TUESDAY -> 2
                    Calendar.WEDNESDAY -> 3
                    Calendar.THURSDAY -> 4
                    Calendar.FRIDAY -> 5
                    Calendar.SATURDAY -> 6
                    Calendar.SUNDAY -> 7
                    else -> 1
                }
                return availability?.weeklyAvailability?.find { it.dayOfWeek == dayOfWeek }?.workDay == true
            }
        }
    }

    // Usiamo key(availability) per forzare il refresh del DatePicker quando i dati arrivano
    val datePickerState = key(availability) {
        rememberDatePickerState(selectableDates = selectableDates)
    }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    
    val availableSlots = viewModel.availableSlots

    LaunchedEffect(service.providerId) {
        viewModel.fetchProviderAvailability(service.providerId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Prenota ${service.name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedDateText.isEmpty()) "Seleziona Data" else selectedDateText)
                }

                if (selectedDateText.isNotEmpty()) {
                    Text("Orari disponibili:", style = MaterialTheme.typography.titleSmall)
                    if (availableSlots.isEmpty()) {
                        Text("Nessuno slot disponibile per questo giorno.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(80.dp),
                            modifier = Modifier.height(200.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableSlots) { slot ->
                                FilterChip(
                                    selected = selectedSlot == slot,
                                    onClick = { selectedSlot = slot },
                                    label = { Text(slot) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedSlot != null) {
                        onConfirm("$selectedDateText $selectedSlot")
                    }
                },
                enabled = selectedSlot != null
            ) {
                Text("Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )

    if (showDatePicker) {
        // Imposta il locale italiano per avere la settimana che inizia di Lunedì
        val configuration = Configuration(LocalConfiguration.current)
        configuration.setLocale(Locale.ITALY)
        
        CompositionLocalProvider(LocalConfiguration provides configuration) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", locale)
                            val date = sdf.format(Date(millis))
                            selectedDateText = date
                            selectedSlot = null
                            viewModel.generateSlots(service, date)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Annulla") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
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
