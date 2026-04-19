package com.example.luontopelibertti71.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.luontopelibertti71.camera.CameraScreen
import com.example.luontopelibertti71.ui.discover.DiscoverScreen
import com.example.luontopelibertti71.ui.map.MapScreen
import com.example.luontopelibertti71.ui.stats.StatsScreen

@Composable
fun LuontopeliNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Map.route, modifier = modifier) {
        composable(Screen.Map.route)      { MapScreen() }
        composable(Screen.Camera.route)   { CameraScreen() }
        composable(Screen.Discover.route) { DiscoverScreen() }
        composable(Screen.Stats.route)    { StatsScreen() }
    }
}