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
    
    private val _availability = mutableStateOf<ProviderAvailability?>(null)
    val availability: State<ProviderAvailability?> = _availability

    private var currentUser: User? = null

    init {
        fetchCurrentUserAndData()
    }

    private fun fetchCurrentUserAndData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                currentUser = doc.toObject(User::class.java)
                fetchMyServices()
                fetchMyAvailability()
            }
    }

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
                    fetchIncomingBookings()
                    fetchReviews(myServices.map { it.id })
                }
            }
    }

    private fun fetchIncomingBookings() {
        db.collection("bookings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val allBookings = snapshot.toObjects(Booking::class.java)
                    val myServiceIds = _services.map { it.id }
                    
                    _incomingBookings.clear()
                    _incomingBookings.addAll(allBookings.filter { myServiceIds.contains(it.serviceId) })
                    calculateStats()
                }
            }
    }

    private fun fetchReviews(serviceIds: List<String>) {
        if (serviceIds.isEmpty()) return
        
        db.collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val allReviews = snapshot.toObjects(Review::class.java)
                    _reviews.clear()
                    _reviews.addAll(allReviews.filter { serviceIds.contains(it.serviceId) })
                }
            }
    }

    private fun fetchMyAvailability() {
        val providerId = auth.currentUser?.uid ?: return
        db.collection("availabilities").document(providerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _availability.value = snapshot.toObject(ProviderAvailability::class.java)
                } else {
                    val default = ProviderAvailability.getDefault(providerId)
                    _availability.value = default
                    // Non salviamo subito per evitare loop, lo stato locale basta finché l'utente non modifica
                }
            }
    }

    fun updateAvailability(newAvailability: ProviderAvailability) {
        val providerId = auth.currentUser?.uid ?: return
        // Aggiornamento ottimistico della UI
        _availability.value = newAvailability
        db.collection("availabilities").document(providerId).set(newAvailability)
    }

    fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("bookings").document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                if (newStatus == "Completed") {
                    calculateStats()
                }
            }
    }

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
}
