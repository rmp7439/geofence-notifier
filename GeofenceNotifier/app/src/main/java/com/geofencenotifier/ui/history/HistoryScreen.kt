package com.geofencenotifier.ui.history
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.ViewModel
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Event
import java.util.Date
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryViewModel(val dao: AppDao) : ViewModel() {
    val events = dao.getEvents()
}

@Composable
fun HistoryScreen(dao: AppDao, onBack: () -> Unit) {
    val viewModel = remember { HistoryViewModel(dao) }
    val events by viewModel.events.collectAsState(initial = emptyList())
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val sdf = SimpleDateFormat("MM/dd HH:mm:ss", Locale.getDefault())
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Event History", style = MaterialTheme.typography.headlineMedium)
            LazyColumn {
                items(events) { evt ->
                    val lName = locs.find { it.id == evt.locationId }?.name ?: "UnknownLoc"
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("$lName - ${evt.transitionType}", style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${evt.status} | Det: ${sdf.format(Date(evt.detectedAt))}")
                            evt.completedAt?.let { Text("Done: ${sdf.format(Date(it))}") }
                        }
                    }
                }
            }
        }
    }
}