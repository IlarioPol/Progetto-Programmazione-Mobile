package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Per favore compila tutti i campi")
            return
        }
        
        _authState.value = AuthState.Loading
        // Mock login logic
        if (email == "test@example.com" && password == "password") {
            _authState.value = AuthState.Success
        } else {
            _authState.value = AuthState.Error("Credenziali non valide")
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = AuthState.Error("Per favore compila tutti i campi")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Le password non coincidono")
            return
        }
        
        _authState.value = AuthState.Loading
        // Mock registration logic
        _authState.value = AuthState.Success
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
