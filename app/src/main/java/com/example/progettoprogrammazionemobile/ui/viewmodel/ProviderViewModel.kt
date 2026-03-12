package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.progettoprogrammazionemobile.data.model.Review
import com.example.progettoprogrammazionemobile.data.model.Service

class ProviderViewModel : ViewModel() {
    private val _services = mutableStateListOf<Service>()
    val services: List<Service> = _services

    private val _reviews = mutableStateListOf<Review>()
    val reviews: List<Review> = _reviews

    private val _totalTurnover = mutableStateOf(0.0)
    val totalTurnover: State<Double> = _totalTurnover

    init {
        // Dati finti per test
        _services.add(Service("1", "Taglio Capelli", "Taglio classico uomo", 30, 20.0, "provider_1"))
        _services.add(Service("2", "Barba", "Regolazione barba e panno caldo", 20, 15.0, "provider_1"))
        
        _reviews.add(Review("1", "1", "Luca G.", 5, "Ottimo servizio!", "2023-10-01"))
        _reviews.add(Review("2", "1", "Paolo B.", 4, "Molto bravo, un po' in ritardo.", "2023-10-05"))
        
        calculateStats()
    }

    fun addService(name: String, description: String, duration: Int, price: Double) {
        val newService = Service(
            id = (_services.size + 1).toString(),
            name = name,
            description = description,
            durationMinutes = duration,
            price = price,
            providerId = "provider_1"
        )
        _services.add(newService)
    }

    private fun calculateStats() {
        // In una app reale, questo verrebbe calcolato dalle prenotazioni completate
        _totalTurnover.value = 450.0 // Mock value
    }
}
