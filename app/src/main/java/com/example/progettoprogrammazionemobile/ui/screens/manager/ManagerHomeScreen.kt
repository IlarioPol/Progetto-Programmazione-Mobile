package com.example.progettoprogrammazionemobile.ui.screens.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
import com.example.progettoprogrammazionemobile.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ManagerViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    var inviteStatus = mutableStateOf<String?>(null)
    var myProviders = mutableStateListOf<String>()

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
            .get()
            .addOnSuccessListener { result ->
                myProviders.clear()
                for (document in result) {
                    myProviders.add(document.getString("name") ?: "Senza nome")
                }
            }
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
    var selectedTab by remember { mutableStateOf(0) }
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
                    label = { Text("Miei Provider") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            if (selectedTab == 0) {
                Text("Professionisti del Team", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (business != null) {
                    Text(text = "Categoria: ${business.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (managerViewModel.myProviders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nessun professionista associato.", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(managerViewModel.myProviders) { providerName ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                ListItem(
                                    headlineContent = { Text(providerName) },
                                    leadingContent = { Icon(Icons.Default.Person, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
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
    
    managerViewModel.inviteStatus.value?.let { status ->
        LaunchedEffect(status) {
            // Snackbar logic would go here
            managerViewModel.inviteStatus.value = null
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
