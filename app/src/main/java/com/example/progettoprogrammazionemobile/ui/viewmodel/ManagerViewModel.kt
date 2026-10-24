package com.example.progettoprogrammazionemobile.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ManagerViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    
    var inviteStatus = mutableStateOf<String?>(null)
    var myProviders = mutableStateListOf<User>()
    
    var totalTurnover = mutableStateOf(0.0)
    var monthlyTurnover = mutableStateOf(0.0)
    var providerStats = mutableStateListOf<ProviderStat>()
    var serviceTrends = mutableStateListOf<ServiceTrend>()
    var averageRating = mutableStateOf(0.0)

    private var currentBookings = listOf<Booking>()
    private var currentServices = listOf<Service>()
    private var currentReviews = listOf<Review>()

    private var bookingsListener: ListenerRegistration? = null
    private var servicesListener: ListenerRegistration? = null
    private var reviewsListener: ListenerRegistration? = null

    var isUploading = mutableStateOf(false)

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
                    val providers = result.toObjects(User::class.java)
                    myProviders.clear()
                    myProviders.addAll(providers)
                    
                    // Se abbiamo dei provider, assicuriamoci che i loro dati siano legati al business
                    if (providers.isNotEmpty()) {
                        syncBusinessDataRetroactively(businessId, providers.map { it.id })
                    }
                    
                    startRealTimeStats(businessId, providers.map { it.id })
                }
            }
    }

    // Funzione fondamentale: corregge i businessId mancanti nel database
    private fun syncBusinessDataRetroactively(businessId: String, providerIds: List<String>) {
        if (providerIds.isEmpty()) return

        // 1. Correggi Servizi
        db.collection("services")
            .whereIn("providerId", providerIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                var needsCommit = false
                for (doc in snapshot.documents) {
                    if (doc.getString("businessId").isNullOrEmpty()) {
                        batch.update(doc.reference, "businessId", businessId)
                        needsCommit = true
                    }
                }
                if (needsCommit) batch.commit()
            }

        // 2. Correggi Prenotazioni
        db.collection("bookings")
            .whereIn("providerId", providerIds)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                var needsCommit = false
                for (doc in snapshot.documents) {
                    if (doc.getString("businessId").isNullOrEmpty()) {
                        batch.update(doc.reference, "businessId", businessId)
                        needsCommit = true
                    }
                }
                if (needsCommit) batch.commit()
            }
    }

    private fun startRealTimeStats(businessId: String, providerIds: List<String>) {
        bookingsListener?.remove()
        servicesListener?.remove()
        reviewsListener?.remove()

        if (providerIds.isEmpty()) return

        // Ascolta le prenotazioni: usiamo providerId come filtro secondario per sicurezza
        bookingsListener = db.collection("bookings")
            .whereIn("providerId", providerIds)
            .addSnapshotListener { snapshot, _ ->
                currentBookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                calculateStats()
            }

        // Ascolta i servizi
        servicesListener = db.collection("services")
            .whereIn("providerId", providerIds)
            .addSnapshotListener { snapshot, _ ->
                currentServices = snapshot?.toObjects(Service::class.java) ?: emptyList()
                
                val serviceIds = currentServices.map { it.id }
                if (serviceIds.isNotEmpty()) {
                    reviewsListener?.remove()
                    reviewsListener = db.collection("reviews")
                        .whereIn("serviceId", serviceIds)
                        .addSnapshotListener { rSnapshot, _ ->
                            currentReviews = rSnapshot?.toObjects(Review::class.java) ?: emptyList()
                            calculateStats()
                        }
                }
                calculateStats()
            }
    }

    private fun calculateStats() {
        val completedBookings = currentBookings.filter { it.status == "Completed" }
        
        var total = 0.0
        var monthly = 0.0
        val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        
        completedBookings.forEach { booking ->
            val service = currentServices.find { it.id == booking.serviceId }
            val price = service?.price ?: 0.0
            total += price
            if (booking.date.contains(currentMonth)) {
                monthly += price
            }
        }
        totalTurnover.value = total
        monthlyTurnover.value = monthly

        val pStats = myProviders.map { provider ->
            val pBookings = completedBookings.filter { it.providerId == provider.id }
            val pTurnover = pBookings.sumOf { b -> currentServices.find { it.id == b.serviceId }?.price ?: 0.0 }
            
            val pServices = currentServices.filter { it.providerId == provider.id }.map { it.id }
            val pReviews = currentReviews.filter { pServices.contains(it.serviceId) }
            val pRating = if (pReviews.isNotEmpty()) pReviews.map { it.rating }.average() else 0.0
            
            ProviderStat(provider.name, pTurnover, pRating)
        }
        providerStats.clear()
        providerStats.addAll(pStats)

        val trends = currentBookings.groupBy { it.serviceName }
            .map { ServiceTrend(it.key, it.value.size) }
            .sortedByDescending { it.count }
            .take(5)
        serviceTrends.clear()
        serviceTrends.addAll(trends)

        averageRating.value = if (currentReviews.isNotEmpty()) currentReviews.map { it.rating }.average() else 0.0
    }

    fun uploadBusinessImage(businessId: String, imageUri: Uri) {
        isUploading.value = true
        val storageRef = storage.reference.child("business_images/$businessId/${UUID.randomUUID()}")
        
        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { url ->
                db.collection("businesses").document(businessId).get().addOnSuccessListener { doc ->
                    val currentUrls = doc.get("imageUrls") as? List<*> ?: emptyList<String>()
                    val updatedUrls = currentUrls.toMutableList()
                    updatedUrls.add(url.toString())
                    db.collection("businesses").document(businessId).update("imageUrls", updatedUrls)
                        .addOnSuccessListener { isUploading.value = false }
                }
            }
        }.addOnFailureListener { isUploading.value = false }
    }

    fun updateBusinessInfo(businessId: String, description: String, address: String) {
        db.collection("businesses").document(businessId).update("description", description, "address", address)
    }
    
    fun deleteImage(businessId: String, imageUrl: String) {
        db.collection("businesses").document(businessId).get().addOnSuccessListener { doc ->
            val currentUrls = doc.get("imageUrls") as? List<*> ?: emptyList<String>()
            val updatedUrls = currentUrls.toMutableList()
            updatedUrls.remove(imageUrl)
            db.collection("businesses").document(businessId).update("imageUrls", updatedUrls).addOnSuccessListener {
                storage.getReferenceFromUrl(imageUrl).delete()
            }
        }
    }

    override fun onCleared() {
        bookingsListener?.remove()
        servicesListener?.remove()
        reviewsListener?.remove()
    }
}
