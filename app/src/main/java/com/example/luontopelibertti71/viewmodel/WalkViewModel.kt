package com.example.luontopelibertti71.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopelibertti71.data.local.AppDatabase
import com.example.luontopelibertti71.data.local.entity.WalkSession
import com.example.luontopelibertti71.sensor.StepCounterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalkViewModel(application: Application) : AndroidViewModel(application) {

    private val stepManager = StepCounterManager(application)
    private val dao = AppDatabase.getDatabase(application).walkSessionDao()

    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    fun startWalk() {
        if (_isWalking.value) return
        _currentSession.value = WalkSession()
        _isWalking.value = true
        //jokainen askel päivittää session askel- ja matkalaskurin
        stepManager.startStepCounting {
            _currentSession.update { it?.copy(
                stepCount = it.stepCount + 1,
                distanceMeters = it.distanceMeters + StepCounterManager.STEP_LENGTH_METERS
            )}
        }
    }

    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false
        _currentSession.update { it?.copy(endTime = System.currentTimeMillis(), isActive = false) }
        //tallennetaan valmis sessio tietokantaan
        viewModelScope.launch {
            _currentSession.value?.let { dao.insert(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}

//muotoilee matkan. alle 1km näytetään metreinä, muuten kilometreinä
fun formatDistance(meters: Float) =
    if (meters < 1000f) "${meters.toInt()} m"
    else "${"%.1f".format(meters / 1000f)} km"

//muotoilee keston tunnit, minuutit tai sekunnit
fun formatDuration(startTime: Long, endTime: Long = System.currentTimeMillis()): String {
    val seconds = (endTime - startTime) / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}min"
        minutes > 0 -> "${minutes}min ${seconds % 60}s"
        else -> "${seconds}s"
    }
}