package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ProviderViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _services = mutableStateListOf<Service>()
    val services: List<Service> = _services

    private val _reviews = mutableStateListOf<Review>()
    val reviews: List<Review> = _reviews

    private val _incomingBookings = mutableStateListOf<Booking>()
    val incomingBookings: List<Booking> = _incomingBookings

    private val _totalTurnover = mutableStateOf(0.0)
    val totalTurnover: State<Double> = _totalTurnover

    init {
        fetchMyServices()
        fetchIncomingBookings()
    }

    fun fetchMyServices() {
        val providerId = auth.currentUser?.uid ?: return
        
        db.collection("services")
            .whereEqualTo("providerId", providerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _services.clear()
                    for (doc in snapshot.documents) {
                        val service = doc.toObject(Service::class.java)
                        if (service != null) {
                            _services.add(service)
                        }
                    }
                    // Una volta caricati i servizi, aggiorna il filtro delle prenotazioni
                    refreshBookingsFilter()
                }
            }
    }

    private fun fetchIncomingBookings() {
        db.collection("bookings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    refreshBookingsFilter(snapshot.documents.mapNotNull { it.toObject(Booking::class.java) })
                }
            }
    }

    private fun refreshBookingsFilter(allBookings: List<Booking>? = null) {
        val bookingsToFilter = allBookings ?: return // Implementazione semplificata per il listener
        
        // Carichiamo tutte le prenotazioni e filtriamo per quelle che appartengono ai servizi di QUESTO provider
        val myServiceIds = _services.map { it.id }
        
        _incomingBookings.clear()
        _incomingBookings.addAll(bookingsToFilter.filter { myServiceIds.contains(it.serviceId) })
    }

    // Sovraccarico per gestire l'aggiornamento automatico
    private fun fetchIncomingBookingsReal() {
        val providerId = auth.currentUser?.uid ?: return
        
        db.collection("bookings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val allBookings = snapshot.documents.mapNotNull { it.toObject(Booking::class.java) }
                    val myServiceIds = _services.map { it.id }
                    
                    _incomingBookings.clear()
                    _incomingBookings.addAll(allBookings.filter { myServiceIds.contains(it.serviceId) })
                }
            }
    }

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                // Il listener ricaricherà i dati
            }
    }

    fun addService(name: String, description: String, duration: Int, price: Double) {
        val providerId = auth.currentUser?.uid ?: return
        val serviceId = UUID.randomUUID().toString()
        
        val newService = Service(
            id = serviceId,
            name = name,
            description = description,
            durationMinutes = duration,
            price = price,
            providerId = providerId
        )
        
        db.collection("services").document(serviceId).set(newService)
    }
}
