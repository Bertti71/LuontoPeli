package com.example.luontopelibertti71.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopelibertti71.data.local.AppDatabase
import com.example.luontopelibertti71.data.local.entity.WalkSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _allSessions = MutableStateFlow<List<WalkSession>>(emptyList())
    val allSessions: StateFlow<List<WalkSession>> = _allSessions.asStateFlow()

    private val _totalSpots = MutableStateFlow(0)
    val totalSpots: StateFlow<Int> = _totalSpots.asStateFlow()

    init {
        // Seurataan molempia tietokantamuutoksia rinnakkain
        viewModelScope.launch {
            db.walkSessionDao().getAllSessions().collect { _allSessions.value = it }
        }
        viewModelScope.launch {
            db.natureSpotDao().getAllSpots().collect { _totalSpots.value = it.size }
        }
    }

    //poistaa kävelylenkin tietokannasta
    fun deleteSession(session: WalkSession) {
        viewModelScope.launch {
            db.walkSessionDao().delete(session)
        }
    }
}