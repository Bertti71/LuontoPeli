package com.example.luontopelibertti71.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun LuontopeliBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Screen.bottomNavScreens.forEach { screen ->
                val selected = currentRoute == screen.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(
                            if (selected) Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            else Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                ) {
                    IconButton(onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }) {
                        Icon(
                            screen.icon,
                            contentDescription = screen.label,
                            tint = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF6B7F96),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = screen.label,
                        fontSize = 11.sp,
                        color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF6B7F96)
                    )
                }
            }
        }
    }
}