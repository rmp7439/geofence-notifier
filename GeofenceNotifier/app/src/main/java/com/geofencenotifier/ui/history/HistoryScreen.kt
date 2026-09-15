package com.geofencenotifier.ui.history
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.ViewModel
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Event
import java.util.Date

class HistoryViewModel(val dao: AppDao) : ViewModel() {
    val events = dao.getEvents()
}

@Composable
fun HistoryScreen(dao: AppDao, onBack: () -> Unit) {
    val viewModel = remember { HistoryViewModel(dao) }
    val events by viewModel.events.collectAsState(initial = emptyList())
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Event History", style = MaterialTheme.typography.headlineMedium)
            LazyColumn {
                items(events) { evt ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Location ID: ${evt.locationId} - ${evt.transitionType}")
                            Text("Status: ${evt.status}")
                            Text("Detected: ${Date(evt.detectedAt)}")
                            evt.completedAt?.let { Text("Completed: ${Date(it)}") }
                        }
                    }
                }
            }
        }
    }
}