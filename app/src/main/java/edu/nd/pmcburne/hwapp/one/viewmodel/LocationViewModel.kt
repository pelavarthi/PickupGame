package edu.nd.pmcburne.hwapp.one.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

class LocationViewModel(app: Application) : AndroidViewModel(app) {

    private val fused = LocationServices.getFusedLocationProviderClient(app)

    private val _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    private val _cityName = MutableStateFlow<String?>(null)
    val cityName = _cityName.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission = _hasPermission.asStateFlow()

    fun refreshPermissionState() {
        val ctx = getApplication<Application>()
        val granted = ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            ctx, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        _hasPermission.value = granted
        if (granted && _location.value == null) fetchLocation()
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        if (!_hasPermission.value) return
        viewModelScope.launch {
            try {
                val loc = fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .await()
                    ?: fused.lastLocation.await()
                if (loc != null) {
                    _location.value = loc
                    reverseGeocode(loc.latitude, loc.longitude)
                }
            } catch (_: Exception) {
                // silently swallow — UI shows fallback text
            }
        }
    }

    private suspend fun reverseGeocode(lat: Double, lng: Double) {
        val ctx = getApplication<Application>()
        val name = withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val results = Geocoder(ctx, Locale.getDefault()).getFromLocation(lat, lng, 1)
                val a = results?.firstOrNull()
                val city = a?.locality ?: a?.subAdminArea ?: a?.adminArea
                val region = a?.adminArea
                if (city != null && region != null && city != region) "$city, $region"
                else city ?: region
            } catch (_: Exception) {
                null
            }
        }
        if (name != null) _cityName.value = name
    }
}
