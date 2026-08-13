package com.example.progettoprogrammazionemobile.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.progettoprogrammazionemobile.data.model.Business
import com.example.progettoprogrammazionemobile.data.model.User
import com.example.progettoprogrammazionemobile.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object VerificationEmailSent : AuthState()
    object PasswordResetSent : AuthState()
    object PasswordUpdated : AuthState()
    object AccountDeleted : AuthState()
    object BusinessCreated : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    private val _pendingInvitation = mutableStateOf<String?>(null)
    val pendingInvitation: State<String?> = _pendingInvitation

    private val _userBusiness = mutableStateOf<Business?>(null)
    val userBusiness: State<Business?> = _userBusiness

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
                    _authState.value = AuthState.Idle // Non bloccare in errore all'avvio
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
                if (user != null) {
                    if (user.isEmailVerified) {
                        fetchUserData(user.uid)
                    } else {
                        _authState.value = AuthState.Error("Email non verificata. Controlla la tua posta.")
                    }
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
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error("Errore salvataggio dati: ${e.localizedMessage}")
                            }
                    }
                    ?.addOnFailureListener { e ->
                        _authState.value = AuthState.Error("Errore invio email: ${e.localizedMessage}")
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
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore reset password")
            }
    }

    fun createBusiness(name: String, category: String, description: String, address: String) {
        val managerId = auth.currentUser?.uid ?: return
        _authState.value = AuthState.Loading
        
        val businessId = UUID.randomUUID().toString()
        val business = Business(
            id = businessId,
            name = name,
            category = category,
            description = description,
            address = address,
            managerId = managerId,
            providerIds = listOf(managerId)
        )

        db.collection("businesses").document(businessId).set(business)
            .addOnSuccessListener {
                db.collection("users").document(managerId).update("businessId", businessId)
                    .addOnSuccessListener {
                        _userBusiness.value = business
                        _authState.value = AuthState.BusinessCreated
                        refreshUserDataSilent(managerId)
                    }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Errore creazione azienda: ${e.localizedMessage}")
            }
    }

    fun updatePassword(newPassword: String) {
        if (newPassword.length < 6) {
            _authState.value = AuthState.Error("La password deve essere di almeno 6 caratteri")
            return
        }
        _authState.value = AuthState.Loading
        auth.currentUser?.updatePassword(newPassword)
            ?.addOnSuccessListener {
                _authState.value = AuthState.PasswordUpdated
            }
            ?.addOnFailureListener { exception ->
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore aggiornamento password")
            }
    }

    fun deleteAccount() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        _authState.value = AuthState.Loading
        
        db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener { _authState.value = AuthState.AccountDeleted }
                    .addOnFailureListener { _authState.value = AuthState.Error("Errore eliminazione account Auth") }
            }
            .addOnFailureListener { _authState.value = AuthState.Error("Errore eliminazione dati database") }
    }

    private fun refreshUserDataSilent(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                document.toObject(User::class.java)?.let { user ->
                    _authState.value = AuthState.Success(user)
                    user.businessId?.let { fetchBusinessData(it) }
                }
            }
    }

    private fun fetchUserData(uid: String) {
        _authState.value = AuthState.Loading
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                document.toObject(User::class.java)?.let { user ->
                    _authState.value = AuthState.Success(user)
                    user.businessId?.let { fetchBusinessData(it) }
                    if (user.role == UserRole.PROVIDER && user.businessId == null) {
                        checkForInvitations(user.email)
                    }
                } ?: run {
                    _authState.value = AuthState.Error("Utente non trovato nel database")
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Errore recupero dati: ${e.localizedMessage}")
            }
    }

    private fun fetchBusinessData(businessId: String) {
        db.collection("businesses").document(businessId).get()
            .addOnSuccessListener { doc ->
                _userBusiness.value = doc.toObject(Business::class.java)
            }
    }

    private fun checkForInvitations(email: String) {
        db.collection("invitations").document(email).get()
            .addOnSuccessListener { doc ->
                if (doc.exists() && doc.getString("status") == "pending") {
                    _pendingInvitation.value = doc.getString("businessId")
                }
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
        _userBusiness.value = null
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
