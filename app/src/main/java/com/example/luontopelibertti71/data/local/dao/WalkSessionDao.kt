package com.example.luontopelibertti71.data.local.dao

import androidx.room.*
import com.example.luontopelibertti71.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WalkSession): Long

    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Query("SELECT * FROM walk_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): WalkSession?

    @Update
    suspend fun update(session: WalkSession)

    @Delete
    suspend fun delete(session: WalkSession)
}