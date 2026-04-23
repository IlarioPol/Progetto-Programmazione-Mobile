package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Business
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ClientViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Liste originali dal DB
    private val _allServices = mutableStateListOf<Service>()
    private val _allBusinesses = mutableStateListOf<Business>()
    
    // Liste filtrate per la UI
    val availableServices = mutableStateListOf<Service>()
    val availableBusinesses = mutableStateListOf<Business>()

    private val _myBookings = mutableStateListOf<Booking>()
    val myBookings: List<Booking> = _myBookings

    var searchQuery = mutableStateOf("")
    var selectedCategory = mutableStateOf("Tutte")

    init {
        fetchAllBusinesses()
        fetchAllServices()
        fetchMyBookings()
    }

    private fun fetchAllBusinesses() {
        db.collection("businesses")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _allBusinesses.clear()
                    _allBusinesses.addAll(snapshot.toObjects(Business::class.java))
                    applyFilters()
                }
            }
    }

    private fun fetchAllServices() {
        db.collection("services")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _allServices.clear()
                    _allServices.addAll(snapshot.toObjects(Service::class.java))
                    applyFilters()
                }
            }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
        applyFilters()
    }

    fun onCategorySelected(category: String) {
        selectedCategory.value = category
        applyFilters()
    }

    private fun applyFilters() {
        val query = searchQuery.value.lowercase()
        val cat = selectedCategory.value

        // 1. Filtra Aziende
        availableBusinesses.clear()
        val filteredBusinesses = _allBusinesses.filter { business ->
            val matchesQuery = business.name.lowercase().contains(query) || 
                             business.description.lowercase().contains(query)
            val matchesCat = cat == "Tutte" || business.category == cat
            matchesQuery && matchesCat
        }
        availableBusinesses.addAll(filteredBusinesses)

        // 2. Filtra Servizi
        availableServices.clear()
        availableServices.addAll(_allServices.filter { service ->
            val business = _allBusinesses.find { it.id == service.businessId }
            
            // Un servizio deve appartenere a un'azienda che rispetta la categoria selezionata
            val matchesCat = cat == "Tutte" || (business != null && business.category == cat)
            
            // E deve rispettare la query di ricerca (nel nome del servizio o nel nome dell'azienda)
            val matchesQuery = service.name.lowercase().contains(query) || 
                             (business != null && business.name.lowercase().contains(query))
            
            matchesCat && matchesQuery
        })
    }

    private fun fetchMyBookings() {
        val clientId = auth.currentUser?.uid ?: return
        db.collection("bookings")
            .whereEqualTo("clientId", clientId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _myBookings.clear()
                    _myBookings.addAll(snapshot.toObjects(Booking::class.java))
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
            status = "Pending"
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
