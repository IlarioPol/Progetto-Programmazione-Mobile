package com.example.progettoprogrammazionemobile.data.model

data class Service(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val durationMinutes: Int = 0,
    val price: Double = 0.0,
    val providerId: String = "",
    val businessId: String = "" // Collega il servizio all'azienda
)
