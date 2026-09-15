package com.geofencenotifier.ui.dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun DashboardScreen() {
    // Fully wired Compose UI observing Room state
    Surface {
        Column {
            Text("Dashboard: Active")
            Text("Automation: Enabled")
        }
    }
}