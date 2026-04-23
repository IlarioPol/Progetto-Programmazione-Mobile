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

    // Stato per l'invito pendente
    private val _pendingInvitation = mutableStateOf<String?>(null) // businessId
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
                        // Aggiorniamo i dati utente senza resettare lo stato a Loading immediatamente
                        refreshUserDataSilent(managerId)
                    }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Errore creazione azienda: ${e.localizedMessage}")
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
                _authState.value = AuthState.Error(exception.localizedMessage ?: "Errore nell'aggiornamento della password.")
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
                            _authState.value = AuthState.Error("Errore nell'eliminazione dell'account.")
                        }
                }
        }
    }

    private fun refreshUserDataSilent(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    _authState.value = AuthState.Success(user)
                    if (user.businessId != null) {
                        fetchBusinessData(user.businessId)
                    }
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
                    if (user.businessId != null) {
                        fetchBusinessData(user.businessId)
                    }
                    if (user.role == UserRole.PROVIDER && user.businessId == null) {
                        checkForInvitations(user.email)
                    }
                }
            }
            .addOnFailureListener { exception ->
                _authState.value = AuthState.Error("Errore nel recupero dati: ${exception.localizedMessage}")
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

    fun acceptInvitation() {
        val businessId = _pendingInvitation.value ?: return
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: return

        db.runTransaction { transaction ->
            val userRef = db.collection("users").document(userId)
            val businessRef = db.collection("businesses").document(businessId)
            val inviteRef = db.collection("invitations").document(email)

            val business = transaction.get(businessRef).toObject(Business::class.java)
            val updatedProviders = (business?.providerIds ?: emptyList()) + userId

            transaction.update(userRef, "businessId", businessId)
            transaction.update(businessRef, "providerIds", updatedProviders)
            transaction.delete(inviteRef)
        }.addOnSuccessListener {
            _pendingInvitation.value = null
            fetchUserData(userId)
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
        _userBusiness.value = null
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
