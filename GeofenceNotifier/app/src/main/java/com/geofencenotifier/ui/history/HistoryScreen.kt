package com.geofencenotifier.ui.history
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun HistoryScreen() {
    val items = listOf("Event 1", "Event 2")
    LazyColumn {
        items(items) { Text("History: $it") }
    }
}