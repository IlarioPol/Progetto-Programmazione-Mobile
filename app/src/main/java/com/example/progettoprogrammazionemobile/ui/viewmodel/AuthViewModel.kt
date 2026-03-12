package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.progettoprogrammazionemobile.data.model.User
import com.example.progettoprogrammazionemobile.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object VerificationEmailSent : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener {
                if (currentUser.isEmailVerified) {
                    fetchUserData(currentUser.uid)
                } else {
                    _authState.value = AuthState.Error("Per favore verifica la tua email")
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Per favore compila tutti i campi")
            return
        }
        
        _authState.value = AuthState.Loading
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null && user.isEmailVerified) {
                    fetchUserData(user.uid)
                } else if (user != null) {
                    _authState.value = AuthState.Error("Email non verificata. Controlla la tua posta.")
                    user.sendEmailVerification()
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore durante il login")
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
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                firebaseUser?.sendEmailVerification()
                    ?.addOnSuccessListener {
                        val userId = firebaseUser.uid
                        val user = User(userId, name, email, role)
                        
                        db.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                _authState.value = AuthState.VerificationEmailSent
                            }
                            .addOnFailureListener { exception ->
                                _authState.value = AuthState.Error("Errore nel salvataggio dei dati: ${exception.localizedMessage}")
                            }
                    }
                    ?.addOnFailureListener { exception ->
                        _authState.value = AuthState.Error("Errore nell'invio dell'email di verifica: ${exception.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore durante la registrazione")
            }
    }

    private fun fetchUserData(uid: String) {
        _authState.value = AuthState.Loading
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Dati utente non trovati")
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error("Errore nel recupero dati: ${exception.localizedMessage}")
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
