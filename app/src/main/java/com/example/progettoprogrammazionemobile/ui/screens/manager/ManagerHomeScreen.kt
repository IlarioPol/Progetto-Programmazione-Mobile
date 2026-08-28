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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.User
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ManagerViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    var inviteStatus = mutableStateOf<String?>(null)
    var myProviders = mutableStateListOf<User>()
    
    // Stats States
    var totalTurnover = mutableStateOf(0.0)
    var monthlyTurnover = mutableStateOf(0.0)
    var providerStats = mutableStateListOf<ProviderStat>()
    var serviceTrends = mutableStateListOf<ServiceTrend>()
    var averageRating = mutableStateOf(0.0)

    data class ProviderStat(val name: String, val turnover: Double, val rating: Double)
    data class ServiceTrend(val name: String, val count: Int)

    fun inviteProvider(email: String, businessId: String?, businessName: String?) {
        if (businessId == null) {
            inviteStatus.value = "Errore: Azienda non configurata"
            return
        }
        
        val managerId = auth.currentUser?.uid ?: return
        val invitation = hashMapOf(
            "email" to email,
            "managerId" to managerId,
            "businessId" to businessId,
            "businessName" to (businessName ?: "Azienda"),
            "status" to "pending"
        )
        
        db.collection("invitations").document(email).set(invitation)
            .addOnSuccessListener {
                inviteStatus.value = "Invito inviato a $email"
            }
            .addOnFailureListener {
                inviteStatus.value = "Errore nell'invio dell'invito"
            }
    }
    
    fun fetchMyProviders(businessId: String?) {
        if (businessId == null) return
        
        db.collection("users")
            .whereEqualTo("businessId", businessId)
            .whereEqualTo("role", "PROVIDER")
            .addSnapshotListener { result, _ ->
                if (result != null) {
                    myProviders.clear()
                    myProviders.addAll(result.toObjects(User::class.java))
                    fetchStatistics(businessId)
                }
            }
    }

    private fun fetchStatistics(businessId: String) {
        // Fetch All Bookings for this business
        db.collection("bookings")
            .whereEqualTo("businessId", businessId)
            .addSnapshotListener { bookingsSnapshot, _ ->
                if (bookingsSnapshot != null) {
                    val bookings = bookingsSnapshot.toObjects(Booking::class.java)
                    
                    // Fetch All Services for this business
                    db.collection("services")
                        .whereEqualTo("businessId", businessId)
                        .get()
                        .addOnSuccessListener { servicesSnapshot ->
                            val services = servicesSnapshot.toObjects(Service::class.java)
                            
                            // Fetch All Reviews for these services
                            val serviceIds = services.map { it.id }
                            if (serviceIds.isNotEmpty()) {
                                db.collection("reviews")
                                    .whereIn("serviceId", serviceIds)
                                    .addSnapshotListener { reviewsSnapshot, _ ->
                                        if (reviewsSnapshot != null) {
                                            val reviews = reviewsSnapshot.toObjects(Review::class.java)
                                            calculateStats(bookings, services, reviews)
                                        }
                                    }
                            } else {
                                calculateStats(bookings, services, emptyList())
                            }
                        }
                }
            }
    }

    private fun calculateStats(bookings: List<Booking>, services: List<Service>, reviews: List<Review>) {
        val completedBookings = bookings.filter { it.status == "Completed" }
        
        // 1. Turnover
        var total = 0.0
        var monthly = 0.0
        val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        
        completedBookings.forEach { booking ->
            val service = services.find { it.id == booking.serviceId }
            val price = service?.price ?: 0.0
            total += price
            if (booking.date.contains(currentMonth)) {
                monthly += price
            }
        }
        totalTurnover.value = total
        monthlyTurnover.value = monthly

        // 2. Provider Stats
        val pStats = myProviders.map { provider ->
            val pBookings = completedBookings.filter { it.providerId == provider.id }
            val pTurnover = pBookings.sumOf { b -> services.find { it.id == b.serviceId }?.price ?: 0.0 }
            
            // Average rating for provider
            val pServices = services.filter { it.providerId == provider.id }.map { it.id }
            val pReviews = reviews.filter { pServices.contains(it.serviceId) }
            val pRating = if (pReviews.isNotEmpty()) pReviews.map { it.rating }.average() else 0.0
            
            ProviderStat(provider.name, pTurnover, pRating)
        }
        providerStats.clear()
        providerStats.addAll(pStats)

        // 3. Service Trends
        val trends = bookings.groupBy { it.serviceName }
            .map { ServiceTrend(it.key, it.value.size) }
            .sortedByDescending { it.count }
            .take(5)
        serviceTrends.clear()
        serviceTrends.addAll(trends)

        // 4. Team Rating
        averageRating.value = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
    }
}

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
                StatCard("Fatturato Totale", "€${String.format("%.2f", viewModel.totalTurnover.value)}", Modifier.weight(1f))
                StatCard("Questo Mese", "€${String.format("%.2f", viewModel.monthlyTurnover.value)}", Modifier.weight(1f))
            }
        }

        // Rating
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rating Medio Team", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%.1f", viewModel.averageRating.value),
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
                        Text("Rating: ${String.format("%.1f", stat.rating)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("€${String.format("%.2f", stat.turnover)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}
