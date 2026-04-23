package com.example.progettoprogrammazionemobile.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CLIENT,
    val managerId: String? = null,
    val businessId: String? = null // Nuovo campo per collegare l'utente (Manager o Provider) a un'azienda
)
