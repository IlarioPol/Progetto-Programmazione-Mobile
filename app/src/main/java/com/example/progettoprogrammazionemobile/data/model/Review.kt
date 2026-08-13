package com.example.progettoprogrammazionemobile.data.model

data class Review(
    val id: String = "",
    val serviceId: String = "",
    val serviceName: String = "", // Aggiunto per facilitare la visualizzazione al provider
    val clientName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val date: String = ""
)
