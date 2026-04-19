package com.example.luontopelibertti71.data.remote.firebase

class StorageManager {
    suspend fun uploadImage(localFilePath: String, spotId: String): Result<String> = Result.success(localFilePath)
    suspend fun deleteImage(spotId: String): Result<Unit> = Result.success(Unit)
}