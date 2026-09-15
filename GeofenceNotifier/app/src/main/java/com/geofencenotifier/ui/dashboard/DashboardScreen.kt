package com.geofencenotifier.ui.dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.*
import com.geofencenotifier.data.db.AppDao

class DashboardViewModel(private val dao: AppDao) : ViewModel() {
    val settings: Flow<AppSettings?> = dao.getSettings()
    val locations = dao.getLocations()
    val recipients = dao.getRecipients()
}

@Composable
fun DashboardScreen(dao: AppDao) {
    val viewModel = DashboardViewModel(dao)
    val settings by viewModel.settings.collectAsState(initial = null)
    val locs by viewModel.locations.collectAsState(initial = emptyList())
    val recps by viewModel.recipients.collectAsState(initial = emptyList())
    
    Surface {
        Column {
            Text("Locations: ${locs.size}")
            Text("Recipients: ${recps.size}")
            Text("System Paused: ${settings?.automationPaused ?: false}")
        }
    }
}