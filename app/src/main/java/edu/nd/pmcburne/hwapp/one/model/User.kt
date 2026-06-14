package edu.nd.pmcburne.hwapp.one.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
