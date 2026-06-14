package edu.nd.pmcburne.hwapp.one.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.nd.pmcburne.hwapp.one.model.Game
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipTextGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreenDark
import edu.nd.pmcburne.hwapp.one.util.Share as ShareUtil
import edu.nd.pmcburne.hwapp.one.util.formatGameDateTime

@Composable
fun GameDetailScreen(
    gameId: String,
    onBack: () -> Unit,
    onChat: (String) -> Unit
) {
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var game by remember { mutableStateOf<Game?>(null) }

    DisposableEffect(gameId) {
        val l = FirebaseFirestore.getInstance().collection("games").document(gameId)
            .addSnapshotListener { snap, _ -> game = snap?.toGame() }
        onDispose { l.remove() }
    }

    val g = game
    if (g == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading…", color = Color.Gray)
        }
        return
    }

    val joined = g.players.contains(currentUserId)
    val isHost = g.creatorId == currentUserId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PickupGreenDark)
                .padding(start = 8.dp, end = 16.dp, top = 36.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.size(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = g.sport,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = formatGameDateTime(g.dateTime),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                IconButton(onClick = { ShareUtil.shareGame(context, g) }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(g.location, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Hosted by ${g.creatorName.ifBlank { "Unknown" }}",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.size(12.dp))
            if (g.hasLocation) {
                val target = LatLng(g.latitude, g.longitude)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(target, 15f)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(zoomControlsEnabled = false)
                    ) {
                        Marker(state = MarkerState(position = target))
                    }
                }
            }

            Spacer(Modifier.size(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChipGreen, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Spots", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        "${g.currentPlayers}/${g.maxPlayers}",
                        color = ChipTextGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Column {
                    Text("Players", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        if (g.isFull) "Full" else "${g.maxPlayers - g.currentPlayers} open",
                        color = ChipTextGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            if (g.notes.isNotBlank()) {
                Spacer(Modifier.size(16.dp))
                Text("Notes", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.size(4.dp))
                Text(g.notes, color = Color.DarkGray, fontSize = 14.sp)
            }

            Spacer(Modifier.size(20.dp))

            if (joined) {
                Button(
                    onClick = { onChat(g.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PickupGreen)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.size(8.dp))
                    Text("Open Chat", fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (!isHost) {
                    Spacer(Modifier.size(12.dp))
                    OutlinedButton(
                        onClick = { leaveGame(g.id, currentUserId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Leave Game", color = Color.Red)
                    }
                }
            } else {
                Button(
                    onClick = { if (!g.isFull) joinGame(g.id, currentUserId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (g.isFull) Color.Gray else PickupGreen
                    ),
                    enabled = !g.isFull
                ) {
                    Text(
                        if (g.isFull) "Full" else "Join Game",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.size(24.dp))
        }
    }
}
