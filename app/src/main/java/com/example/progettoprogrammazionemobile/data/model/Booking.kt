package com.example.progettoprogrammazionemobile.data.model

data class Booking(
    val id: String = "",
    val serviceId: String = "",
    val providerId: String = "",
    val businessId: String = "", // Aggiunto per facilitare le statistiche del manager
    val serviceName: String = "",
    val clientId: String = "",
    val date: String = "", // Formato "dd/MM/yyyy HH:mm"
    val status: String = "Pending"
)
