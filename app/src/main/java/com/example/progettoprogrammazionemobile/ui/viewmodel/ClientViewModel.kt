package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Booking
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service
import java.util.UUID

class ClientViewModel : ViewModel() {
    private val _availableServices = mutableStateListOf<Service>()
    val availableServices: List<Service> = _availableServices

    private val _myBookings = mutableStateListOf<Booking>()
    val myBookings: List<Booking> = _myBookings

    init {
        // Mock services
        _availableServices.add(Service("1", "Taglio Capelli", "Taglio classico uomo", 30, 20.0, "provider_1"))
        _availableServices.add(Service("2", "Barba", "Regolazione barba e panno caldo", 20, 15.0, "provider_1"))
        _availableServices.add(Service("3", "Massaggio Relax", "Massaggio corpo 60 minuti", 60, 50.0, "provider_2"))
        
        // Mock initial booking
        _myBookings.add(Booking("b1", "1", "Taglio Capelli", "client_1", "2023-11-20 10:00", "Confermata"))
    }

    fun bookService(service: Service, date: String) {
        val newBooking = Booking(
            id = UUID.randomUUID().toString(),
            serviceId = service.id,
            serviceName = service.name,
            clientId = "client_1", // In real app, get from current user
            date = date,
            status = "In attesa"
        )
        _myBookings.add(newBooking)
    }

    fun addReview(serviceId: String, serviceName: String, rating: Int, comment: String) {
        // In a real app, this would save to a database
        // For now, we just simulate the action
        println("Recensione aggiunta per $serviceName: $rating stelle - $comment")
    }
}
