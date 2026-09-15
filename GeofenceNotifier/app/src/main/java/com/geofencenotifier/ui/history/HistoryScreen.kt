package com.geofencenotifier.ui.history
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.ViewModel
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Event

class HistoryViewModel(val dao: AppDao) : ViewModel() {
    val events = dao.getEvents()
}

@Composable
fun HistoryScreen(dao: AppDao) {
    val viewModel = HistoryViewModel(dao)
    val events by viewModel.events.collectAsState(initial = emptyList())
    LazyColumn {
        items(events) { evt ->
            Text("Event: ${evt.transitionType} at ${evt.detectedAt} - Status: ${evt.status}")
        }
    }
}