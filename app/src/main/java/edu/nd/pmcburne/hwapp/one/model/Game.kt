package edu.nd.pmcburne.hwapp.one.model

import com.google.firebase.Timestamp

data class Game(
    val id: String = "",
    val sport: String = "",
    val location: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val dateTime: Timestamp = Timestamp.now(),
    val maxPlayers: Int = 0,
    val players: List<String> = emptyList(),
    val creatorId: String = "",
    val creatorName: String = "",
    val notes: String = ""
) {
    val currentPlayers: Int get() = players.size
    val isFull: Boolean get() = currentPlayers >= maxPlayers
    val hasLocation: Boolean get() = latitude != 0.0 || longitude != 0.0
}
