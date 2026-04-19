package com.example.luontopelibertti71.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopelibertti71.data.local.AppDatabase
import com.example.luontopelibertti71.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).natureSpotDao()

    private val _allSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val allSpots: StateFlow<List<NatureSpot>> = _allSpots.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAllSpots().collect { _allSpots.value = it }
        }
    }

    fun deleteSpot(spot: NatureSpot) {
        viewModelScope.launch {
            dao.delete(spot)
        }
    }
}