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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.nd.pmcburne.hwapp.one.model.Message
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.ChipTextGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreenDark
import edu.nd.pmcburne.hwapp.one.util.formatChatTime

@Composable
fun ChatScreen(
    gameId: String,
    onBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance().currentUser
    val currentUserId = auth?.uid ?: ""
    val db = FirebaseFirestore.getInstance()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var sportLabel by remember { mutableStateOf("Chat") }
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    DisposableEffect(gameId) {
        val gameSub = db.collection("games").document(gameId)
            .addSnapshotListener { snap, _ ->
                snap?.getString("sport")?.let { sportLabel = "$it Chat" }
            }
        val msgSub = db.collection("games").document(gameId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                messages = snap.documents.map { d ->
                    Message(
                        id = d.id,
                        senderId = d.getString("senderId") ?: "",
                        senderName = d.getString("senderName") ?: "",
                        text = d.getString("text") ?: "",
                        timestamp = d.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                }
            }
        onDispose {
            gameSub.remove()
            msgSub.remove()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PickupGreenDark)
                    .padding(start = 4.dp, end = 16.dp, top = 36.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = sportLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text("Message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                IconButton(
                    onClick = {
                        val text = draft.trim()
                        if (text.isNotEmpty() && currentUserId.isNotBlank()) {
                            val name = auth?.displayName?.takeIf { it.isNotBlank() }
                                ?: auth?.email?.substringBefore("@")
                                ?: "Player"
                            db.collection("games").document(gameId)
                                .collection("messages").add(
                                    mapOf(
                                        "senderId" to currentUserId,
                                        "senderName" to name,
                                        "text" to text,
                                        "timestamp" to FieldValue.serverTimestamp()
                                    )
                                )
                            draft = ""
                        }
                    },
                    enabled = draft.isNotBlank()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (draft.isBlank()) Color.Gray else PickupGreen
                    )
                }
            }
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No messages yet — say hi!",
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { m ->
                    MessageBubble(m, isMine = m.senderId == currentUserId)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!isMine) {
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMine) PickupGreen else ChipGreen,
                        shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = if (isMine) 14.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 14.dp
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else ChipTextGreen,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatChatTime(message.timestamp),
                        color = if (isMine) Color.White.copy(alpha = 0.7f)
                        else Color.Gray.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
