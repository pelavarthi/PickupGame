package edu.nd.pmcburne.hwapp.one.ui.screens

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.nd.pmcburne.hwapp.one.model.Game
import edu.nd.pmcburne.hwapp.one.ui.components.BottomNavBar
import edu.nd.pmcburne.hwapp.one.ui.components.Tab
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipTextGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreenDark
import edu.nd.pmcburne.hwapp.one.util.Share as ShareUtil
import edu.nd.pmcburne.hwapp.one.util.distanceMilesString
import edu.nd.pmcburne.hwapp.one.util.formatGameDateTime
import edu.nd.pmcburne.hwapp.one.viewmodel.LocationViewModel

@Composable
fun HomeScreen(
    locationViewModel: LocationViewModel,
    onCreateGame: () -> Unit,
    onProfile: () -> Unit,
    onOpenGame: (String) -> Unit
) {
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val userLocation by locationViewModel.location.collectAsState()
    val cityName by locationViewModel.cityName.collectAsState()
    val hasPermission by locationViewModel.hasPermission.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> locationViewModel.refreshPermissionState() }

    LaunchedEffect(Unit) {
        locationViewModel.refreshPermissionState()
        if (!locationViewModel.hasPermission.value) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(Unit) {
        val listener = FirebaseFirestore.getInstance()
            .collection("games")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null || snapshot == null) return@addSnapshotListener
                games = snapshot.documents.mapNotNull { doc -> doc.toGame() }
            }
        onDispose { listener.remove() }
    }

    val filteredAndSorted = run {
        val q = searchQuery.trim()
        val filtered = if (q.isBlank()) games else games.filter {
            it.sport.contains(q, ignoreCase = true) ||
                it.location.contains(q, ignoreCase = true)
        }
        if (userLocation != null) filtered.sortedBy { distanceMeters(userLocation!!, it) ?: Float.MAX_VALUE }
        else filtered
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = Tab.Home,
                onHomeClick = {},
                onProfileClick = onProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PickupGreenDark,
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Nearby Games",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = when {
                                    cityName != null -> cityName!!
                                    !hasPermission -> "Location off"
                                    else -> "Locating\u2026"
                                },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        FloatingActionButton(
                            onClick = onCreateGame,
                            containerColor = PickupGreen,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Create Game")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search sport or location...",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.4f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            cursorColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.15f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(color = PickupGreen)
                }
            } else if (filteredAndSorted.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = if (searchQuery.isBlank())
                            "No games yet. Be the first to create one!"
                        else "No games match your search.",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                Text(
                    text = "Open Games",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAndSorted) { game ->
                        GameCard(
                            game = game,
                            userLocation = userLocation,
                            onClick = { onOpenGame(game.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    userLocation: Location?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val alreadyJoined = game.players.contains(currentUserId)
    val distanceText = userLocation?.let { loc ->
        distanceMeters(loc, game)?.let { distanceMilesString(it) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(ChipGreen, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = game.sport,
                        color = ChipTextGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${game.currentPlayers}/${game.maxPlayers} spots",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    IconButton(
                        onClick = { ShareUtil.shareGame(context, game) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share game",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (distanceText != null) "${game.location} \u00B7 $distanceText away"
                else game.location,
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatGameDateTime(game.dateTime),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Button(
                    onClick = {
                        if (!alreadyJoined && !game.isFull) {
                            joinGame(game.id, currentUserId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alreadyJoined) Color.Gray else PickupGreen
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !alreadyJoined && !game.isFull,
                    modifier = Modifier.width(80.dp)
                ) {
                    Text(
                        text = when {
                            alreadyJoined -> "Joined"
                            game.isFull -> "Full"
                            else -> "Join"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

internal fun joinGame(gameId: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val gameRef = db.collection("games").document(gameId)
    db.runTransaction { transaction ->
        val snapshot = transaction.get(gameRef)
        val players = (snapshot.get("players") as? List<*>)
            ?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        val maxPlayers = (snapshot.getLong("maxPlayers") ?: 0).toInt()
        if (!players.contains(userId) && players.size < maxPlayers) {
            players.add(userId)
            transaction.update(gameRef, "players", players)
        }
    }
}

internal fun leaveGame(gameId: String, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val gameRef = db.collection("games").document(gameId)
    db.runTransaction { transaction ->
        val snapshot = transaction.get(gameRef)
        val players = (snapshot.get("players") as? List<*>)
            ?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        if (players.remove(userId)) {
            transaction.update(gameRef, "players", players)
        }
    }
}

internal fun distanceMeters(userLocation: Location, game: Game): Float? {
    if (!game.hasLocation) return null
    val results = FloatArray(1)
    Location.distanceBetween(
        userLocation.latitude, userLocation.longitude,
        game.latitude, game.longitude,
        results
    )
    return results[0]
}

internal fun com.google.firebase.firestore.DocumentSnapshot.toGame(): Game? = try {
    Game(
        id = id,
        sport = getString("sport") ?: "",
        location = getString("location") ?: "",
        latitude = getDouble("latitude") ?: 0.0,
        longitude = getDouble("longitude") ?: 0.0,
        dateTime = getTimestamp("dateTime") ?: Timestamp.now(),
        maxPlayers = (getLong("maxPlayers") ?: 0).toInt(),
        players = (get("players") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        creatorId = getString("creatorId") ?: "",
        creatorName = getString("creatorName") ?: "",
        notes = getString("notes") ?: ""
    )
} catch (_: Exception) {
    null
}
