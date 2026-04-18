package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ClientViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _availableServices = mutableStateListOf<Service>()
    val availableServices: List<Service> = _availableServices

    private val _myBookings = mutableStateListOf<Booking>()
    val myBookings: List<Booking> = _myBookings

    init {
        fetchAllServices()
        fetchMyBookings()
    }

    private fun fetchAllServices() {
        db.collection("services")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _availableServices.clear()
                    for (doc in snapshot.documents) {
                        val service = doc.toObject(Service::class.java)
                        if (service != null) {
                            _availableServices.add(service)
                        }
                    }
                }
            }
    }

    private fun fetchMyBookings() {
        val clientId = auth.currentUser?.uid ?: return
        
        db.collection("bookings")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _myBookings.clear()
                    for (doc in snapshot.documents) {
                        val booking = doc.toObject(Booking::class.java)
                        if (booking != null) {
                            _myBookings.add(booking)
                        }
                    }
                }
            }
    }

    fun bookService(service: Service, date: String) {
        val clientId = auth.currentUser?.uid ?: return
        val bookingId = UUID.randomUUID().toString()
        
        val newBooking = Booking(
            id = bookingId,
            serviceId = service.id,
            serviceName = service.name,
            clientId = clientId,
            date = date,
            status = "Pending" // Sincronizzato con il Provider
        )
        
        db.collection("bookings").document(bookingId).set(newBooking)
    }

    fun addReview(serviceId: String, serviceName: String, rating: Int, comment: String) {
        val clientId = auth.currentUser?.uid ?: return
        val reviewId = UUID.randomUUID().toString()
        
        val newReview = Review(
            id = reviewId,
            serviceId = serviceId,
            clientName = auth.currentUser?.displayName ?: "Cliente",
            rating = rating,
            comment = comment,
            date = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        )
        
        db.collection("reviews").document(reviewId).set(newReview)
    }
}
