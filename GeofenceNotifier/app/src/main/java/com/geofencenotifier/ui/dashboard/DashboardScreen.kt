package com.geofencenotifier.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Dashboard: Coming in Phase 10")
        Button(onClick = { navController.navigate("locations") }) { Text("Locations") }
        Button(onClick = { navController.navigate("recipients") }) { Text("Recipients") }
        Button(onClick = { navController.navigate("rules") }) { Text("Notification Rules") }
        Button(onClick = { navController.navigate("history") }) { Text("Event History") }
        Button(onClick = { navController.navigate("settings") }) { Text("Settings") }
        Button(onClick = { navController.navigate("testmode") }) { Text("Test Mode") }
    }
}
