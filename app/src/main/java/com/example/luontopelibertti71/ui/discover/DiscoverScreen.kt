package com.example.luontopelibertti71.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.luontopelibertti71.data.local.entity.NatureSpot
import com.example.luontopelibertti71.viewmodel.DiscoverViewModel
import com.example.luontopelibertti71.viewmodel.toFormattedDate
import java.io.File

@Composable
fun DiscoverScreen(viewModel: DiscoverViewModel = viewModel()) {
    val spots by viewModel.allSpots.collectAsState()

    // Poistovarmistus-dialogi
    var spotToDelete by remember { mutableStateOf<NatureSpot?>(null) }
    spotToDelete?.let { spot ->
        AlertDialog(
            onDismissRequest = { spotToDelete = null },
            title = { Text("Poista löytö") },
            text = { Text("Haluatko varmasti poistaa tämän löydön?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSpot(spot)
                    spotToDelete = null
                }) { Text("Kyllä", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { spotToDelete = null }) { Text("Ei") }
            }
        )
    }

    if (spots.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Explore, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("Ei löytöjä vielä", modifier = Modifier.padding(8.dp))
                Text("Ota kuva kasveista kameralla!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("${spots.size} löytöä", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
            }
            items(spots, key = { it.id }) { spot ->
                NatureSpotCard(spot = spot, onDelete = { spotToDelete = spot })
            }
        }
    }
}

@Composable
fun NatureSpotCard(spot: NatureSpot, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.padding(12.dp)) {
            val imageModel = spot.imageFirebaseUrl ?: spot.imageLocalPath?.let { File(it) }
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = spot.plantLabel,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Explore, null) }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = spot.plantLabel ?: "Tuntematon",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (spot.synced) Icons.Default.Cloud else Icons.Default.CloudOff,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = if (spot.synced) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                spot.confidence?.let { conf ->
                    Text(
                        "${"%.0f".format(conf * 100)}% varmuus",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conf > 0.8f) Color(0xFF4A90D9) else Color.Gray
                    )
                }
                Text(spot.timestamp.toFormattedDate(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // Poistopainike
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Poista", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}