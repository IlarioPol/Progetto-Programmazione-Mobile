package com.example.progettoprogrammazionemobile.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.progettoprogrammazionemobile.data.model.Business
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.User
import com.example.progettoprogrammazionemobile.ui.viewmodel.ClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    businessId: String,
    onBack: () -> Unit,
    onBookService: (Service) -> Unit,
    viewModel: ClientViewModel = viewModel()
) {
    val businesses = viewModel.availableBusinesses
    val business = businesses.find { it.id == businessId }
    
    val allServices = viewModel.availableServices
    val businessServices = allServices.filter { it.businessId == businessId }

    // State for team members
    var teamMembers by remember { mutableStateOf<List<User>>(emptyList()) }
    
    LaunchedEffect(businessId) {
        // Fetch team members (users with businessId == businessId and role PROVIDER)
        viewModel.fetchTeamMembers(businessId) { members ->
            teamMembers = members
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(business?.name ?: "Dettagli Azienda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (business == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Galleria Immagini
                if (business.imageUrls.isNotEmpty()) {
                    item {
                        Text("Galleria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(business.imageUrls) { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(200.dp, 150.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Descrizione e Info
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Informazioni", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(business.description)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Indirizzo: ${business.address}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("Categoria: ${business.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Team
                if (teamMembers.isNotEmpty()) {
                    item {
                        Text("Il Nostro Team", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(teamMembers) { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(member.name, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Catalogo Servizi
                item {
                    Text("Catalogo Servizi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                
                if (businessServices.isEmpty()) {
                    item {
                        Text("Nessun servizio disponibile al momento.", color = Color.Gray)
                    }
                } else {
                    items(businessServices) { service ->
                        ServiceCardDetail(service, onBookService)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCardDetail(service: Service, onBookClick: (Service) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("€${service.price} • ${service.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Button(onClick = { onBookClick(service) }) {
                Text("Prenota")
            }
        }
    }
}
