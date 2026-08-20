package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Business
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import com.example.progettoprogrammazionemobile.data.model.ProviderAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class ClientViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _allServices = mutableStateListOf<Service>()
    private val _allBusinesses = mutableStateListOf<Business>()
    
    val availableServices = mutableStateListOf<Service>()
    val availableBusinesses = mutableStateListOf<Business>()

    private val _myBookings = mutableStateListOf<Booking>()
    val myBookings: List<Booking> = _myBookings

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _selectedCategory = mutableStateOf("Tutte")
    val selectedCategory: State<String> = _selectedCategory
    
    private val _selectedProviderAvailability = mutableStateOf<ProviderAvailability?>(null)
    val selectedProviderAvailability: State<ProviderAvailability?> = _selectedProviderAvailability

    private val _availableSlots = mutableStateListOf<String>()
    val availableSlots: List<String> = _availableSlots

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
        _searchQuery.value = newQuery
        applyFilters()
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val cat = _selectedCategory.value

        availableBusinesses.clear()
        val filteredBusinesses = _allBusinesses.filter { business ->
            val matchesQuery = business.name.lowercase().contains(query) || 
                             business.description.lowercase().contains(query)
            val matchesCat = cat == "Tutte" || business.category == cat
            matchesQuery && matchesCat
        }
        availableBusinesses.addAll(filteredBusinesses)

        availableServices.clear()
        availableServices.addAll(_allServices.filter { service ->
            val business = _allBusinesses.find { it.id == service.businessId }
            val matchesCat = cat == "Tutte" || (business != null && business.category == cat)
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

    fun fetchProviderAvailability(providerId: String) {
        db.collection("availabilities").document(providerId).get()
            .addOnSuccessListener { doc ->
                _selectedProviderAvailability.value = doc.toObject(ProviderAvailability::class.java)
            }
    }

    fun generateSlots(service: Service, date: String) {
        _availableSlots.clear()
        val availability = _selectedProviderAvailability.value ?: return
        
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val parsedDate = try { sdf.parse(date) } catch (e: Exception) { null } ?: return
        val cal = Calendar.getInstance()
        cal.time = parsedDate
        
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val dayAvail = availability.weeklyAvailability.find { it.dayOfWeek == dayOfWeek }
        if (dayAvail == null || !dayAvail.workDay) return

        // Interroghiamo TUTTE le prenotazioni del provider, non solo quelle di questo servizio
        db.collection("bookings")
            .whereEqualTo("providerId", service.providerId)
            .get()
            .addOnSuccessListener { snapshot ->
                val existingBookings = snapshot.toObjects(Booking::class.java)
                    .filter { it.date.startsWith(date) && it.status != "Canceled" && it.status != "Rejected" }
                
                val slots = mutableListOf<String>()
                val timeSdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                
                val now = Calendar.getInstance()
                val isToday = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(now.time) == date

                dayAvail.timeRanges.forEach { range ->
                    val startCal = Calendar.getInstance().apply { 
                        time = timeSdf.parse(range.startTime)!!
                        val d = Calendar.getInstance().apply { time = parsedDate }
                        set(Calendar.YEAR, d.get(Calendar.YEAR))
                        set(Calendar.MONTH, d.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, d.get(Calendar.DAY_OF_MONTH))
                    }
                    val endCal = Calendar.getInstance().apply { 
                        time = timeSdf.parse(range.endTime)!!
                        val d = Calendar.getInstance().apply { time = parsedDate }
                        set(Calendar.YEAR, d.get(Calendar.YEAR))
                        set(Calendar.MONTH, d.get(Calendar.MONTH))
                        set(Calendar.DAY_OF_MONTH, d.get(Calendar.DAY_OF_MONTH))
                    }

                    while (startCal.timeInMillis + service.durationMinutes * 60000 <= endCal.timeInMillis) {
                        if (isToday && startCal.before(now)) {
                            startCal.add(Calendar.MINUTE, 30)
                            continue
                        }

                        val slotTime = timeSdf.format(startCal.time)
                        val slotDateTime = "$date $slotTime"
                        
                        // Lo slot è occupato se esiste una qualsiasi prenotazione per questo provider a quest'ora
                        val isOccupied = existingBookings.any { it.date == slotDateTime }
                        if (!isOccupied) {
                            slots.add(slotTime)
                        }
                        
                        startCal.add(Calendar.MINUTE, 30) 
                    }
                }
                _availableSlots.clear()
                _availableSlots.addAll(slots)
            }
    }

    fun bookService(service: Service, date: String) {
        val clientId = auth.currentUser?.uid ?: return
        val bookingId = UUID.randomUUID().toString()
        val newBooking = Booking(
            id = bookingId,
            serviceId = service.id,
            providerId = service.providerId, // Colleghiamo la prenotazione al professionista
            serviceName = service.name,
            clientId = clientId,
            date = date,
            status = "Pending"
        )
        db.collection("bookings").document(bookingId).set(newBooking)
    }

    fun cancelBooking(bookingId: String) {
        db.collection("bookings").document(bookingId).update("status", "Canceled")
    }

    fun addReview(serviceId: String, serviceName: String, rating: Int, comment: String) {
        val clientId = auth.currentUser?.uid ?: return
        val reviewId = UUID.randomUUID().toString()
        val clientName = auth.currentUser?.displayName ?: "Cliente"

        val newReview = Review(
            id = reviewId,
            serviceId = serviceId,
            serviceName = serviceName,
            clientName = clientName,
            rating = rating,
            comment = comment,
            date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        )
        db.collection("reviews").document(reviewId).set(newReview)
    }
}
