package com.example.progettoprogrammazionemobile.data.model

data class Booking(
    val id: String = "",
    val serviceId: String = "",
    val providerId: String = "", // Aggiunto per controllare l'agenda del professionista
    val serviceName: String = "",
    val clientId: String = "",
    val date: String = "",
    val status: String = "Pending"
)
