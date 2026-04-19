package com.example.luontopelibertti71.data.remote.firebase

import com.example.luontopelibertti71.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FirestoreManager {
    suspend fun saveSpot(spot: NatureSpot): Result<Unit> = Result.success(Unit)
    fun getUserSpots(userId: String): Flow<List<NatureSpot>> = flowOf(emptyList())
}