package com.example.luontopelibertti71.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

//hallitsee GPS-sijaintia, käyttää androidin omaa LocationManager-luokkaa

class LocationManager(context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    //nykyinen sijainti. null kunnes ensimmäinen sijainti saadaan
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    //lista kävelyn reittipisteistä, kasvaa jokaisen sijaintipäivityksen myötä
    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    // Kutsutaan automaattisesti kun laite saa uuden sijaintitiedon
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location
            _routePoints.value = _routePoints.value + GeoPoint(location.latitude, location.longitude)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    //aloittaa sijaintiseurannan
    //päivittää vähintään 5s tai 10m välein
    @SuppressLint("MissingPermission")
    fun startTracking() {
        val provider = when {
            locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ->
                android.location.LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ->
                android.location.LocationManager.NETWORK_PROVIDER
            else -> return
        }
        locationManager.requestLocationUpdates(provider, 5000L, 10f, locationListener)
    }

    //pysäyttää sijaintiseurannan
    fun stopTracking() = locationManager.removeUpdates(locationListener)

    //tyhjentää reitin,  kutsutaan kun uusi kävely aloitetaan
    fun resetRoute() { _routePoints.value = emptyList() }
}