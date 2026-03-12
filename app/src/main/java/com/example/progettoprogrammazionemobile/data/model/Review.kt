package com.example.progettoprogrammazionemobile.data.model

data class Review(
    val id: String,
    val serviceId: String,
    val clientName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val date: String
)
