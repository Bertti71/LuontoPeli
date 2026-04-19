package com.example.luontopelibertti71.ui.map

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luontopelibertti71.viewmodel.MapViewModel
import com.example.luontopelibertti71.viewmodel.WalkViewModel
import com.example.luontopelibertti71.viewmodel.formatDuration
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    walkViewModel: WalkViewModel = viewModel()
) {
    val context = LocalContext.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        Configuration.getInstance().userAgentValue = context.packageName
    }

    if (!permissionState.allPermissionsGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Sijaintilupa tarvitaan karttaa varten")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Myönnä lupa")
            }
        }
        return
    }

    val isWalking by walkViewModel.isWalking.collectAsState()
    val routePoints by mapViewModel.routePoints.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val session by walkViewModel.currentSession.collectAsState()

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var compassBearing by remember { mutableStateOf(0f) }

    LaunchedEffect(isWalking) {
        if (isWalking) {
            mapViewModel.startTracking()
        } else {
            mapViewModel.stopTracking()
            mapViewModel.resetRoute()
        }
        while (isWalking) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    val defaultPosition = GeoPoint(65.0121, 25.4651)
    val mapViewState = remember { MapView(context) }

    // Sijaintioverlay — personBitmap näytetään paikallaan, arrowBitmap liikkuessa
    val myLocationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapViewState).also { overlay ->
            overlay.enableMyLocation()
        }
    }

    DisposableEffect(Unit) {
        // Kompassi — lukee laitteen suunnan gyroskoopilla reaaliajassa
        val sensorManager = context.getSystemService(android.content.Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val sensor = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ROTATION_VECTOR)
        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent) {
                val rotationMatrix = FloatArray(9)
                android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
                compassBearing = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }
            override fun onAccuracyChanged(sensor: android.hardware.Sensor, accuracy: Int) {}
        }
        sensor?.let {
            sensorManager.registerListener(listener, it, android.hardware.SensorManager.SENSOR_DELAY_UI)
        }

        mapViewState.setTileSource(TileSourceFactory.MAPNIK)
        mapViewState.setMultiTouchControls(true)
        mapViewState.controller.setZoom(15.0)
        mapViewState.controller.setCenter(defaultPosition)
        mapViewState.zoomController.setVisibility(
            org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
        )
        onDispose {
            sensorManager.unregisterListener(listener)
            mapViewState.onDetach()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { mapViewState },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                // Päivitetään bitmap kompassin suunnalla joka kerta
                val size = 48
                val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
                android.graphics.Canvas(bitmap).apply {
                    // Valkoinen reunus
                    drawCircle(size / 2f, size / 2f, size / 2f, android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        isAntiAlias = true
                    })
                    // Sininen piste
                    drawCircle(size / 2f, size / 2f, size / 2f - 6f, android.graphics.Paint().apply {
                        color = 0xFF4A90D9.toInt()
                        isAntiAlias = true
                    })
                    // Nuoli — käännetään kompassin mukaan
                    save()
                    rotate(compassBearing, size / 2f, size / 2f)
                    drawPath(android.graphics.Path().apply {
                        moveTo(size / 2f, 2f)
                        lineTo(size / 2f - 10f, size / 2f)
                        lineTo(size / 2f + 10f, size / 2f)
                        close()
                    }, android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        isAntiAlias = true
                        style = android.graphics.Paint.Style.FILL
                    })
                    restore()
                }
                myLocationOverlay.setPersonIcon(bitmap)
                myLocationOverlay.setDirectionArrow(bitmap, bitmap)

                mapView.overlays.clear()
                mapView.overlays.add(myLocationOverlay)
                if (routePoints.size >= 2) {
                    mapView.overlays.add(Polyline().apply {
                        setPoints(routePoints)
                        outlinePaint.color = 0xFF4A90D9.toInt()
                        outlinePaint.strokeWidth = 8f
                    })
                }
                mapView.invalidate()
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = { mapViewState.controller.zoomIn() },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
            FloatingActionButton(
                onClick = { mapViewState.controller.zoomOut() },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text("−", style = MaterialTheme.typography.titleLarge)
            }
            FloatingActionButton(
                onClick = {
                    myLocationOverlay.myLocation?.let { geoPoint ->
                        mapViewState.controller.animateTo(geoPoint)
                        mapViewState.controller.setZoom(17.0)
                    } ?: currentLocation?.let {
                        mapViewState.controller.animateTo(GeoPoint(it.latitude, it.longitude))
                        mapViewState.controller.setZoom(17.0)
                    }
                },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Keskitä sijaintiin", modifier = Modifier.size(20.dp))
            }
        }

        if (isWalking && session != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Kävely: ${formatDuration(session!!.startTime, currentTime)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
        ) {
            if (!isWalking) {
                ExtendedFloatingActionButton(
                    onClick = { walkViewModel.startWalk() },
                    icon = { Icon(Icons.Default.DirectionsWalk, null) },
                    text = { Text("Aloita kävely") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            } else {
                ExtendedFloatingActionButton(
                    onClick = { walkViewModel.stopWalk() },
                    icon = { Icon(Icons.Default.DirectionsWalk, null) },
                    text = { Text("Lopeta kävely") },
                    containerColor = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}