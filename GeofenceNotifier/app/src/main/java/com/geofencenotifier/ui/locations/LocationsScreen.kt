package com.geofencenotifier.ui.locations
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao

@Composable
fun LocationsScreen(dao: AppDao) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    LazyColumn {
        items(locs) { loc -> Text("Location: ${loc.name} - ${loc.latitude}, ${loc.longitude}") }
    }
}