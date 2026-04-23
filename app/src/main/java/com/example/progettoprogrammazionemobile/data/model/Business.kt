package com.example.progettoprogrammazionemobile.data.model

data class Business(
    val id: String = "",
    val name: String = "",
    val category: String = "", // Macro-categoria (es. Beauty, Legal, Medical)
    val description: String = "",
    val address: String = "",
    val managerId: String = "",
    val providerIds: List<String> = emptyList()
)
