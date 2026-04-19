package com.example.luontopelibertti71.data.repository

import com.example.luontopelibertti71.data.local.dao.NatureSpotDao
import com.example.luontopelibertti71.data.local.entity.NatureSpot
import com.example.luontopelibertti71.data.remote.firebase.AuthManager
import com.example.luontopelibertti71.data.remote.firebase.FirestoreManager
import com.example.luontopelibertti71.data.remote.firebase.StorageManager
import kotlinx.coroutines.flow.Flow

class NatureSpotRepository(
    private val dao: NatureSpotDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val authManager: AuthManager
) {
    val allSpots: Flow<List<NatureSpot>> = dao.getAllSpots()
    val spotsWithLocation: Flow<List<NatureSpot>> = dao.getSpotsWithLocation()

    suspend fun insertSpot(spot: NatureSpot) {
        val spotWithUser = spot.copy(userId = authManager.currentUserId, synced = true)
        dao.insert(spotWithUser)
    }

    suspend fun deleteSpot(spot: NatureSpot) {
        dao.delete(spot)
    }
}