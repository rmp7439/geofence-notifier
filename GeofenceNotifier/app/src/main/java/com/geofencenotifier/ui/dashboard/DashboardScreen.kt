package com.geofencenotifier.ui.dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.*
import com.geofencenotifier.data.db.AppDao

class DashboardViewModel(private val dao: AppDao) : ViewModel() {
    val settings: Flow<AppSettings?> = dao.getSettings()
    val locations = dao.getLocations()
    val recipients = dao.getRecipients()
    val rules = dao.getRules()
    val events = dao.getEvents()
}

@Composable
fun DashboardScreen(dao: AppDao, onNavigate: (String) -> Unit) {
    val viewModel = remember { DashboardViewModel(dao) }
    val settings by viewModel.settings.collectAsState(initial = null)
    val locs by viewModel.locations.collectAsState(initial = emptyList())
    val recps by viewModel.recipients.collectAsState(initial = emptyList())
    val rules by viewModel.rules.collectAsState(initial = emptyList())
    val evts by viewModel.events.collectAsState(initial = emptyList())
    
    val activeLocs = locs.count { it.active }
    val paused = settings?.automationPaused ?: false
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("System Status", style = MaterialTheme.typography.titleMedium)
                    Text(if (paused) "AUTOMATION PAUSED" else "AUTOMATION RUNNING")
                    Text("Active Locations: $activeLocs / ${locs.size}")
                    Text("Recipients: ${recps.size}")
                    Text("Rules: ${rules.size}")
                    Text("Total Events: ${evts.size}")
                    Text("Boot Status: ${settings?.lastBootRegistrationResult ?: "N/A"}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigate("settings") }) { Text("Settings") }
            Button(onClick = { onNavigate("locations") }) { Text("Locations") }
            Button(onClick = { onNavigate("recipients") }) { Text("Recipients") }
            Button(onClick = { onNavigate("rules") }) { Text("Rules") }
            Button(onClick = { onNavigate("history") }) { Text("History") }
            Button(onClick = { onNavigate("testmode") }) { Text("Test Mode") }
        }
    }
}