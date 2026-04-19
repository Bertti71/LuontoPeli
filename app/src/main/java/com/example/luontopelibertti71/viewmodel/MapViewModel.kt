package com.example.luontopelibertti71.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import com.example.luontopelibertti71.location.LocationManager
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.*

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val locationManager = LocationManager(application)

    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    fun startTracking() = locationManager.startTracking()
    fun stopTracking() = locationManager.stopTracking()
    fun resetRoute() = locationManager.resetRoute()

    override fun onCleared() {
        super.onCleared()
        locationManager.stopTracking()
    }
}

//muotoilee aikaleiman luettavaan muotoon
fun Long.toFormattedDate(): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(this))