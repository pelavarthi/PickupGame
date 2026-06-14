package edu.nd.pmcburne.hwapp.one.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        if (auth.currentUser != null) {
            _authState.value = AuthState(isLoggedIn = true)
            upsertUserDoc()
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState(errorMessage = "Please fill in all fields")
            return
        }
        _authState.value = AuthState(isLoading = true)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                upsertUserDoc()
                _authState.value = AuthState(isLoggedIn = true)
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState(errorMessage = e.message ?: "Sign in failed")
            }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState(errorMessage = "Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState(errorMessage = "Password must be at least 6 characters")
            return
        }
        _authState.value = AuthState(isLoading = true)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                upsertUserDoc()
                _authState.value = AuthState(isLoggedIn = true)
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState(errorMessage = e.message ?: "Sign up failed")
            }
    }

    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _authState.value = AuthState(
                errorMessage = "Google sign-in not configured. Set web_client_id in strings.xml."
            )
            return
        }
        _authState.value = AuthState(isLoading = true)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                upsertUserDoc()
                _authState.value = AuthState(isLoggedIn = true)
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState(errorMessage = e.message ?: "Google sign-in failed")
            }
    }

    fun updateDisplayName(name: String, onDone: (Boolean) -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: run { onDone(false); return }
        db.collection("users").document(uid)
            .set(mapOf("displayName" to name), SetOptions.merge())
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(errorMessage = null)
    }

    fun setError(message: String) {
        _authState.value = _authState.value.copy(errorMessage = message, isLoading = false)
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState()
    }

    private fun upsertUserDoc() {
        val u = auth.currentUser ?: return
        val ref = db.collection("users").document(u.uid)
        val baseDisplay = u.displayName?.takeIf { it.isNotBlank() }
            ?: u.email?.substringBefore("@")
            ?: "Player"
        ref.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                ref.set(
                    mapOf(
                        "uid" to u.uid,
                        "displayName" to baseDisplay,
                        "email" to (u.email ?: ""),
                        "photoUrl" to (u.photoUrl?.toString() ?: ""),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                val updates = mutableMapOf<String, Any>()
                u.email?.let { updates["email"] = it }
                u.photoUrl?.toString()?.let { if (it.isNotBlank()) updates["photoUrl"] = it }
                if (snap.getTimestamp("createdAt") == null) {
                    updates["createdAt"] = FieldValue.serverTimestamp()
                }
                if (updates.isNotEmpty()) ref.set(updates, SetOptions.merge())
            }
        }
    }
}
