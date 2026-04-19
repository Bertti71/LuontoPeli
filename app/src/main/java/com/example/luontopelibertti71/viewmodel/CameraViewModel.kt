package com.example.luontopelibertti71.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopelibertti71.data.local.AppDatabase
import com.example.luontopelibertti71.data.local.entity.NatureSpot
import com.example.luontopelibertti71.data.remote.firebase.AuthManager
import com.example.luontopelibertti71.data.remote.firebase.FirestoreManager
import com.example.luontopelibertti71.data.remote.firebase.StorageManager
import com.example.luontopelibertti71.data.repository.NatureSpotRepository
import com.example.luontopelibertti71.ml.ClassificationResult
import com.example.luontopelibertti71.ml.PlantClassifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NatureSpotRepository(
        dao = AppDatabase.getDatabase(application).natureSpotDao(),
        firestoreManager = FirestoreManager(),
        storageManager = StorageManager(),
        authManager = AuthManager()
    )
    private val classifier = PlantClassifier()

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    //GPS-sijainti asetetaan MapViewModelista ennen tallennusta
    var currentLatitude: Double = 0.0
    var currentLongitude: Double = 0.0

    //ottaa kuvan ja käynnistää ML Kit tunnistuksen automaattisesti
    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true
        val outputFile = File(
            File(context.filesDir, "nature_photos").also { it.mkdirs() },
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        )

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(outputFile).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _capturedImagePath.value = outputFile.absolutePath
                    viewModelScope.launch {
                        _classificationResult.value = try {
                            classifier.classify(Uri.fromFile(outputFile), context)
                        } catch (e: Exception) {
                            ClassificationResult.Error(e.message ?: "Tuntematon virhe")
                        }
                        _isLoading.value = false
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    _isLoading.value = false
                }
            }
        )
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        val result = _classificationResult.value
        viewModelScope.launch {
            repository.insertSpot(NatureSpot(
                name = (result as? ClassificationResult.Success)?.label ?: "Luontolöytö",
                latitude = currentLatitude,
                longitude = currentLongitude,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence
            ))
            clearCapturedImage()
        }
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}