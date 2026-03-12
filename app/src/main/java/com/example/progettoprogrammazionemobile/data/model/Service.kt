package com.example.progettoprogrammazionemobile.data.model

data class Service(
    val id: String = "",
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val price: Double,
    val providerId: String
)
