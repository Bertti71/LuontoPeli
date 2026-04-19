package com.example.luontopelibertti71

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.luontopelibertti71.ui.navigation.LuontopeliBottomBar
import com.example.luontopelibertti71.ui.navigation.LuontopeliNavHost
import com.example.luontopelibertti71.ui.theme.LuontopeliTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            LuontopeliTheme { LuontopeliApp() }
        }
    }
}

@Composable
fun LuontopeliApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { LuontopeliBottomBar(navController) }
    ) { innerPadding ->
        LuontopeliNavHost(navController, modifier = Modifier.padding(innerPadding))
    }
}