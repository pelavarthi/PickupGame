package edu.nd.pmcburne.hwapp.one.model

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
