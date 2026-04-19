package com.example.luontopelibertti71.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luontopelibertti71.data.local.entity.WalkSession
import com.example.luontopelibertti71.viewmodel.StatsViewModel
import com.example.luontopelibertti71.viewmodel.formatDistance
import com.example.luontopelibertti71.viewmodel.formatDuration
import com.example.luontopelibertti71.viewmodel.toFormattedDate

@Composable
fun StatsScreen(viewModel: StatsViewModel = viewModel()) {
    val sessions by viewModel.allSessions.collectAsState()
    val totalSpots by viewModel.totalSpots.collectAsState()

    val totalSteps = sessions.sumOf { it.stepCount }
    val totalDistance = sessions.sumOf { it.distanceMeters.toDouble() }.toFloat()

    // Poistovarmistus-dialogi
    var sessionToDelete by remember { mutableStateOf<WalkSession?>(null) }
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Poista lenkki") },
            text = { Text("Haluatko varmasti poistaa tämän kävelylenkin?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSession(session)
                    sessionToDelete = null
                }) { Text("Kyllä", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) { Text("Ei") }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Tilastot", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 8.dp))
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatSummaryCard(value = "$totalSteps", label = "Askelta yhteensä", modifier = Modifier.weight(1f))
                StatSummaryCard(value = formatDistance(totalDistance), label = "Matka yhteensä", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatSummaryCard(value = "$totalSpots", label = "Löytöjä", modifier = Modifier.weight(1f))
                StatSummaryCard(value = "${sessions.size}", label = "Kävelylenkkejä", modifier = Modifier.weight(1f))
            }
        }

        if (sessions.isNotEmpty()) {
            item {
                Text("Kävelyhistoria", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
            }
            items(sessions, key = { it.id }) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsWalk, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${session.stepCount} askelta • ${formatDistance(session.distanceMeters)}", style = MaterialTheme.typography.titleSmall)
                            Text(session.startTime.toFormattedDate(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            session.endTime?.let { end ->
                                Text("Kesto: ${formatDuration(session.startTime, end)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        // Poistopainike
                        IconButton(onClick = { sessionToDelete = session }) {
                            Icon(Icons.Default.Delete, contentDescription = "Poista", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BarChart, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("Ei kävelylenkkejä vielä", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatSummaryCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}