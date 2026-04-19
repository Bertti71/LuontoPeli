package com.example.luontopelibertti71.data.remote.firebase

import java.util.UUID

class AuthManager {
    private val localUserId: String = UUID.randomUUID().toString()
    val currentUserId: String get() = localUserId
    val isSignedIn: Boolean get() = true

    suspend fun signInAnonymously(): Result<String> = Result.success(localUserId)
    fun signOut() {}
}