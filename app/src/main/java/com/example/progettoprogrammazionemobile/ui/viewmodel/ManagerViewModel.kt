package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.User
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
