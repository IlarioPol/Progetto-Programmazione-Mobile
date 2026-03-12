package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.progettoprogrammazionemobile.data.model.User
import com.example.progettoprogrammazionemobile.data.model.UserRole

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
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
        
        // Mock logic per differenziare i ruoli nei test
        when (email) {
            "cliente@test.com" if password == "password" -> {
                _authState.value = AuthState.Success(User("1", "Mario Rossi", email, UserRole.CLIENT))
            }
            "provider@test.com" if password == "password" -> {
                _authState.value = AuthState.Success(User("2", "Centro Servizi", email, UserRole.PROVIDER))
            }
            else -> {
                _authState.value = AuthState.Error("Credenziali non valide")
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String, role: UserRole) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _authState.value = AuthState.Error("Per favore compila tutti i campi")
            return
        }
        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Le password non coincidono")
            return
        }
        
        _authState.value = AuthState.Loading
        // Simulazione registrazione con ruolo scelto
        _authState.value = AuthState.Success(User("3", name, email, role))
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
