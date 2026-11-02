package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.User
import com.example.progettoprogrammazionemobile.data.model.ProviderAvailability
import com.example.progettoprogrammazionemobile.data.model.DayAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ProviderViewModel : ViewModel() {
    // =================================================================================
    // ISTANZE FIREBASE
    // =================================================================================
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // =================================================================================
    // STATI OSSERVABILI (UI STATE)
    // =================================================================================
    private val _services = mutableStateListOf<Service>()
    val services: List<Service> = _services

    private val _reviews = mutableStateListOf<Review>()
    val reviews: List<Review> = _reviews

    private val _incomingBookings = mutableStateListOf<Booking>()
    val incomingBookings: List<Booking> = _incomingBookings

    private val _totalTurnover = mutableStateOf(0.0)
    val totalTurnover: State<Double> = _totalTurnover
    
    private val _availability = mutableStateOf<ProviderAvailability?>(null)
    val availability: State<ProviderAvailability?> = _availability

    // Dati utente correntemente loggato (Provider)
    private var currentUser: User? = null

    init {
        fetchCurrentUserAndData()
    }

    // =================================================================================
    // 1. RECUPERO DATI IN TEMPO REALE (LISTENERS)
    // =================================================================================

    /**
     * Recupera il profilo utente e attiva i listener per servizi e disponibilità.
     * Ascolta anche cambiamenti al profilo (es. associazione a un Business).
     */
    private fun fetchCurrentUserAndData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                val user = doc.toObject(User::class.java)
                currentUser = user
                
                // Sincronizzazione automatica: se il provider è in un business, aggiorna i servizi orfani
                user?.businessId?.let { bId ->
                    updateServicesBusinessId(bId)
                }
                
                fetchMyServices()
                fetchMyAvailability()
            }
        }
    }

    /**
     * Carica i servizi creati dal provider.
     * Al caricamento dei servizi, attiva i listener per le relative prenotazioni e recensioni.
     */
    fun fetchMyServices() {
        val providerId = auth.currentUser?.uid ?: return
        
        db.collection("services")
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _services.clear()
                    val myServices = snapshot.toObjects(Service::class.java)
                    _services.addAll(myServices)
                    
                    // Carica i dati dipendenti dai servizi
                    fetchIncomingBookings()
                    fetchReviews(myServices.map { it.id })
                }
            }
    }

    /**
     * Ascolta tutte le prenotazioni indirizzate a questo provider.
     */
    private fun fetchIncomingBookings() {
        val providerId = auth.currentUser?.uid ?: return
        db.collection("bookings")
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _incomingBookings.clear()
                    _incomingBookings.addAll(snapshot.toObjects(Booking::class.java))
                    calculateStats() // Ricalcola il fatturato ad ogni cambio stato
                }
            }
    }

    /**
     * Recupera le recensioni per i servizi offerti dal provider.
     */
    private fun fetchReviews(serviceIds: List<String>) {
        if (serviceIds.isEmpty()) return
        
        db.collection("reviews")
            .whereIn("serviceId", serviceIds)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _reviews.clear()
                    _reviews.addAll(snapshot.toObjects(Review::class.java))
                }
            }
    }

    /**
     * Recupera la tabella oraria (disponibilità) del provider.
     */
    private fun fetchMyAvailability() {
        val providerId = auth.currentUser?.uid ?: return
        db.collection("availabilities").document(providerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _availability.value = snapshot.toObject(ProviderAvailability::class.java)
                } else {
                    // Imposta una disponibilità predefinita se non esiste ancora nel DB
                    _availability.value = ProviderAvailability.getDefault(providerId)
                }
            }
    }

    // =================================================================================
    // 2. AZIONI E LOGICA DI BUSINESS
    // =================================================================================

    /**
     * Aggiorna gli orari di lavoro del provider.
     */
    fun updateAvailability(newAvailability: ProviderAvailability) {
        val providerId = auth.currentUser?.uid ?: return
        _availability.value = newAvailability
        db.collection("availabilities").document(providerId).set(newAvailability)
    }

    /**
     * Cambia lo stato di una prenotazione (es. Accetta, Rifiuta, Completa).
     */
    fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
    }

    /**
     * Aggiunge un nuovo servizio al catalogo.
     * Se il provider è legato a un business, lo associa automaticamente.
     */
    fun addService(name: String, description: String, duration: Int, price: Double) {
        val providerId = auth.currentUser?.uid ?: return
        val bId = currentUser?.businessId ?: ""
        val serviceId = UUID.randomUUID().toString()
        
        val newService = Service(
            id = serviceId,
            name = name,
            description = description,
            durationMinutes = duration,
            price = price,
            providerId = providerId,
            businessId = bId
        )
        
        db.collection("services").document(serviceId).set(newService)
    }

    fun updateService(serviceId: String, name: String, description: String, duration: Int, price: Double) {
        db.collection("services").document(serviceId)
            .update(
                "name", name,
                "description", description,
                "durationMinutes", duration,
                "price", price
            )
    }

    fun deleteService(serviceId: String) {
        db.collection("services").document(serviceId).delete()
    }

    // =================================================================================
    // 3. UTILITY E CALCOLI
    // =================================================================================

    /**
     * Calcola il fatturato totale basandosi solo sulle prenotazioni "Completed".
     */
    private fun calculateStats() {
        var total = 0.0
        val completedBookings = _incomingBookings.filter { it.status == "Completed" }
        
        for (booking in completedBookings) {
            val service = _services.find { it.id == booking.serviceId }
            if (service != null) {
                total += service.price
            }
        }
        _totalTurnover.value = total
    }

    /**
     * Corregge retroattivamente il businessId dei servizi se il provider è stato appena aggiunto a un'azienda.
     */
    private fun updateServicesBusinessId(businessId: String) {
        val providerId = auth.currentUser?.uid ?: return
        db.collection("services")
            .whereEqualTo("providerId", providerId)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val currentBId = doc.getString("businessId")
                    if (currentBId.isNullOrEmpty()) {
                        doc.reference.update("businessId", businessId)
                    }
                }
            }
    }
}
