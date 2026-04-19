package com.example.luontopelibertti71.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luontopelibertti71.ml.ClassificationResult
import com.example.luontopelibertti71.viewmodel.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val capturedImagePath by viewModel.capturedImagePath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val classificationResult by viewModel.classificationResult.collectAsState()

    //pyydetään kameralupa
    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("Kameran lupa tarvitaan", modifier = Modifier.padding(8.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Myönnä lupa")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        //näytetään joko kameran esikatselu tai otettu kuva
        if (capturedImagePath == null) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    ProcessCameraProvider.getInstance(ctx).also { future ->
                        future.addListener({
                            val preview = Preview.Builder().build()
                                .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                            future.get().apply {
                                unbindAll()
                                bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.BottomCenter) {
                FloatingActionButton(
                    onClick = { viewModel.takePhoto(context, imageCapture) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Icon(Icons.Default.Camera, "Ota kuva", tint = Color.White)
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = File(capturedImagePath!!),
                    contentDescription = "Otettu kuva",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
                classificationResult?.let { ClassificationResultCard(it) }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = { viewModel.clearCapturedImage() }) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ota uudelleen")
                    }
                    Button(onClick = { viewModel.saveCurrentSpot() }) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tallenna löytö")
                    }
                }
            }
        }
    }
}

@Composable
fun ClassificationResultCard(result: ClassificationResult) {
    val confidenceColor = { conf: Float -> if (conf > 0.8f) Color(0xFF2E7D32) else Color(0xFFF57C00) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result is ClassificationResult.Success)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (result) {
                is ClassificationResult.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Tunnistettu:", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${"%.0f".format(result.confidence * 100)}%",
                            color = confidenceColor(result.confidence),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(result.label, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 4.dp))
                    LinearProgressIndicator(
                        progress = { result.confidence },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = confidenceColor(result.confidence)
                    )
                }
                is ClassificationResult.NotNature ->
                    Text("Ei luontokohde — tunnistettiin: ${result.allLabels.joinToString { it.text }}", style = MaterialTheme.typography.bodySmall)
                is ClassificationResult.Error ->
                    Text("Tunnistus epäonnistui: ${result.message}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}