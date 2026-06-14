package edu.nd.pmcburne.hwapp.one.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import edu.nd.pmcburne.hwapp.one.util.formatGameDateTime
import edu.nd.pmcburne.hwapp.one.util.formatMonthYear
import edu.nd.pmcburne.hwapp.one.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onHome: () -> Unit,
    onOpenGame: (String) -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var displayName by remember { mutableStateOf("") }
    var memberSince by remember { mutableStateOf<Timestamp?>(null) }
    var games by remember { mutableStateOf<List<Game>>(emptyList()) }
    var editing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }

    DisposableEffect(uid) {
        val userListener = FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                displayName = snap.getString("displayName") ?: "Player"
                memberSince = snap.getTimestamp("createdAt")
            }
        val gamesListener = FirebaseFirestore.getInstance().collection("games")
            .whereArrayContains("players", uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                games = snap.documents.mapNotNull { it.toGame() }
                    .sortedByDescending { it.dateTime.seconds }
            }
        onDispose {
            userListener.remove()
            gamesListener.remove()
        }
    }

    val joined = games.size
    val hosted = games.count { it.creatorId == uid }
    val sportsCount = games.map { it.sport }.distinct().size

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentTab = Tab.Profile,
                onHomeClick = onHome,
                onProfileClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(PickupGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayName.initials(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName.ifBlank { "Player" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "Member since ${memberSince?.let { formatMonthYear(it) } ?: "—"}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                editName = displayName
                                editing = true
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Edit", color = Color.White, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.size(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatChip(joined.toString(), "Joined", Modifier.weight(1f))
                        StatChip(hosted.toString(), "Hosted", Modifier.weight(1f))
                        StatChip(sportsCount.toString(), "Sports", Modifier.weight(1f))
                    }
                }
            }

            Text(
                text = "My Games",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )

            if (games.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No games yet — join or create one from Home.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(games) { g ->
                        MyGameRow(g, isHost = g.creatorId == uid) { onOpenGame(g.id) }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    ShareUtil.shareProfile(context, displayName, joined, hosted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Share Profile",
                    color = PickupGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = {
                    authViewModel.signOut()
                    onSignedOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Sign out", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit display name") },
            text = {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    singleLine = true,
                    placeholder = { Text("Your name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editName.isNotBlank()) {
                        authViewModel.updateDisplayName(editName.trim())
                    }
                    editing = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editing = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatChip(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun MyGameRow(game: Game, isHost: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ChipGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = game.sport.firstOrNull()?.toString() ?: "?",
                    color = ChipTextGreen,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(game.sport, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${game.location} \u00B7 ${formatGameDateTime(game.dateTime)}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
            val tagBg = if (isHost) ChipGreen else Color(0xFFEDE7F6)
            val tagText = if (isHost) ChipTextGreen else Color(0xFF6A1B9A)
            Box(
                modifier = Modifier
                    .background(tagBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isHost) "Host" else "Joined",
                    color = tagText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun String.initials(): String {
    if (isBlank()) return "?"
    val parts = trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
        else -> parts[0].take(2).uppercase()
    }
}
