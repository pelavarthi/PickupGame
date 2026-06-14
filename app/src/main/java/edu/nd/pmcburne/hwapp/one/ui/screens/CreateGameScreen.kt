package edu.nd.pmcburne.hwapp.one.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreen
import edu.nd.pmcburne.hwapp.one.ui.theme.PickupGreenDark
import edu.nd.pmcburne.hwapp.one.viewmodel.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val SPORTS = listOf(
    "Basketball", "Soccer", "Volleyball", "Tennis", "Football", "Frisbee", "Pickleball"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGameScreen(
    locationViewModel: LocationViewModel,
    onBack: () -> Unit,
    onPosted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userLocation by locationViewModel.location.collectAsState()

    var sport by remember { mutableStateOf("") }
    var sportExpanded by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("") }
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var dateMillis by remember { mutableStateOf<Long?>(null) }
    var hour by remember { mutableStateOf<Int?>(null) }
    var minute by remember { mutableStateOf<Int?>(null) }
    var maxPlayersInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var posting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val fallback = remember { LatLng(38.0336, -78.5080) } // UVA / Charlottesville
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fallback, 14f)
    }
    val markerState = remember { MarkerState(position = fallback) }

    LaunchedEffect(userLocation) {
        if (pickedLatLng == null && userLocation != null) {
            val ll = LatLng(userLocation!!.latitude, userLocation!!.longitude)
            pickedLatLng = ll
            markerState.position = ll
            cameraPositionState.position = CameraPosition.fromLatLngZoom(ll, 14f)
            if (locationText.isBlank()) {
                val name = withContext(Dispatchers.IO) {
                    reverseGeocodeShort(context, ll.latitude, ll.longitude)
                }
                if (name != null) locationText = name
            }
        }
    }

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
                .padding(start = 8.dp, end = 20.dp, top = 36.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.padding(horizontal = 4.dp))
                Column {
                    Text(
                        text = "Create a Game",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Fill in the details below",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {

            FieldLabel("SPORT")
            ExposedDropdownMenuBox(
                expanded = sportExpanded,
                onExpandedChange = { sportExpanded = !sportExpanded }
            ) {
                OutlinedTextField(
                    value = sport,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select a sport...") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = sportExpanded,
                    onDismissRequest = { sportExpanded = false }
                ) {
                    SPORTS.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                sport = s
                                sportExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            FieldLabel("LOCATION")
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                placeholder = { Text("Search or pick on map...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false),
                    onMapClick = { latLng ->
                        pickedLatLng = latLng
                        markerState.position = latLng
                        scope.launch {
                            val name = withContext(Dispatchers.IO) {
                                reverseGeocodeShort(context, latLng.latitude, latLng.longitude)
                            }
                            if (name != null) locationText = name
                        }
                    }
                ) {
                    if (pickedLatLng != null) {
                        Marker(state = markerState, draggable = true)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("DATE")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        cal.set(y, m, d, 0, 0, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        dateMillis = cal.timeInMillis
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = dateMillis?.let {
                                SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(it)
                            } ?: "",
                            onValueChange = {},
                            placeholder = { Text("mm/dd/yy") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
                Spacer(Modifier.padding(horizontal = 6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("TIME")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, h, m -> hour = h; minute = m },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    false
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = if (hour != null && minute != null)
                                String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                            else "",
                            onValueChange = {},
                            placeholder = { Text("hh:mm") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            FieldLabel("MAX PLAYERS")
            OutlinedTextField(
                value = maxPlayersInput,
                onValueChange = { v -> maxPlayersInput = v.filter { it.isDigit() }.take(2) },
                placeholder = { Text("e.g. 10") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel("NOTES (optional)")
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Any extra info...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                shape = RoundedCornerShape(12.dp)
            )

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(text = error!!, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val ll = pickedLatLng
                    val dm = dateMillis
                    val h = hour
                    val mi = minute
                    val cap = maxPlayersInput.toIntOrNull() ?: 0
                    when {
                        sport.isBlank() -> error = "Pick a sport"
                        locationText.isBlank() -> error = "Add a location"
                        ll == null -> error = "Tap the map to set a spot"
                        dm == null -> error = "Pick a date"
                        h == null || mi == null -> error = "Pick a time"
                        cap < 2 -> error = "Need at least 2 players"
                        else -> {
                            error = null
                            posting = true
                            postGame(
                                sport = sport,
                                location = locationText,
                                lat = ll.latitude,
                                lng = ll.longitude,
                                dateMillis = dm,
                                hour = h,
                                minute = mi,
                                maxPlayers = cap,
                                notes = notes,
                                onDone = { ok ->
                                    posting = false
                                    if (ok) onPosted() else error = "Failed to post"
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PickupGreen),
                enabled = !posting
            ) {
                Text(
                    text = if (posting) "Posting..." else "Post Game",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color.DarkGray,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

private fun postGame(
    sport: String,
    location: String,
    lat: Double,
    lng: Double,
    dateMillis: Long,
    hour: Int,
    minute: Int,
    maxPlayers: Int,
    notes: String,
    onDone: (Boolean) -> Unit
) {
    val auth = FirebaseAuth.getInstance().currentUser ?: run { onDone(false); return }
    val cal = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
    }
    val displayName = auth.displayName?.takeIf { it.isNotBlank() }
        ?: auth.email?.substringBefore("@")
        ?: "Player"
    val data = hashMapOf(
        "sport" to sport,
        "location" to location,
        "latitude" to lat,
        "longitude" to lng,
        "dateTime" to Timestamp(cal.time),
        "maxPlayers" to maxPlayers,
        "players" to listOf(auth.uid),
        "creatorId" to auth.uid,
        "creatorName" to displayName,
        "notes" to notes
    )
    FirebaseFirestore.getInstance().collection("games").add(data)
        .addOnSuccessListener { onDone(true) }
        .addOnFailureListener { onDone(false) }
}

private fun reverseGeocodeShort(context: Context, lat: Double, lng: Double): String? {
    return try {
        @Suppress("DEPRECATION")
        val res = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
        val a = res?.firstOrNull() ?: return null
        a.featureName?.takeIf { it.any(Char::isLetter) }
            ?: a.thoroughfare
            ?: a.locality
            ?: a.subAdminArea
    } catch (_: Exception) { null }
}
