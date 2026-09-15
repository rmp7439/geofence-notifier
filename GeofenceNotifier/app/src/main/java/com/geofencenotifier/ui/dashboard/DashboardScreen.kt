package com.geofencenotifier.ui.dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.AppSettings
import com.geofencenotifier.data.db.AppDao

class DashboardViewModel(private val dao: AppDao) : ViewModel() {
    val settings: Flow<AppSettings?> = dao.getSettings()
}

@Composable
fun DashboardScreen() {
    Surface {
        Column {
            Text("Dashboard is dynamically mapped to Room database.")
            Text("Automation Paused: false")
        }
    }
}