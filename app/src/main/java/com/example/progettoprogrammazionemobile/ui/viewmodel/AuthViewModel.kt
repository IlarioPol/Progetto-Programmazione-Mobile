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
    object PasswordResetSent : AuthState()
    object PasswordUpdated : AuthState()
    object AccountDeleted : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // Stato per l'invito pendente
    private val _pendingInvitation = mutableStateOf<String?>(null) // managerId
    val pendingInvitation: State<String?> = _pendingInvitation

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

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Per favore inserisci la tua email")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _authState.value = AuthState.PasswordResetSent
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore nell'invio dell'email di reset")
            }
    }

    fun updatePassword(newPassword: String) {
        if (newPassword.isBlank() || newPassword.length < 6) {
            _authState.value = AuthState.Error("La password deve essere di almeno 6 caratteri")
            return
        }

        _authState.value = AuthState.Loading
        val user = auth.currentUser
        user?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                _authState.value = AuthState.PasswordUpdated
            }
            ?.addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore nell'aggiornamento della password. Potrebbe essere necessario rieffettuare il login.")
            }
    }

    fun deleteAccount() {
        _authState.value = AuthState.Loading
        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            db.collection("users").document(userId).delete()
                .addOnSuccessListener {
                    user.delete()
                        .addOnSuccessListener {
                            _authState.value = AuthState.AccountDeleted
                        }
                        .addOnFailureListener { exception ->
                            _authState.value = AuthState.Error("Errore nell'eliminazione dell'account: ${exception.localizedMessage}. Prova a rieffettuare il login.")
                        }
                }
                .addOnFailureListener { exception ->
                    _authState.value = AuthState.Error("Errore nell'eliminazione dei dati: ${exception.localizedMessage}")
                }
        }
    }

    private fun fetchUserData(uid: String) {
        _authState.value = AuthState.Loading
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _authState.value = AuthState.Success(user)
                    // Se è un provider senza manager, controlla gli inviti
                    if (user.role == UserRole.PROVIDER && user.managerId == null) {
                        checkForInvitations(user.email)
                    }
                } else {
                    _authState.value = AuthState.Error("Dati utente non trovati")
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error("Errore nel recupero dati: ${exception.localizedMessage}")
            }
    }

    private fun checkForInvitations(email: String) {
        db.collection("invitations").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.getString("status") == "pending") {
                    _pendingInvitation.value = document.getString("managerId")
                }
            }
    }

    fun acceptInvitation() {
        val managerId = _pendingInvitation.value ?: return
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: return

        _authState.value = AuthState.Loading
        
        // 1. Aggiorna l'utente con il managerId
        db.collection("users").document(userId).update("managerId", managerId)
            .addOnSuccessListener {
                // 2. Rimuovi l'invito
                db.collection("invitations").document(email).delete()
                _pendingInvitation.value = null
                fetchUserData(userId) // Ricarica i dati
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error("Errore nell'accettazione: ${exception.localizedMessage}")
            }
    }

    fun declineInvitation() {
        val email = auth.currentUser?.email ?: return
        db.collection("invitations").document(email).delete()
        _pendingInvitation.value = null
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _pendingInvitation.value = null
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
