package com.geofencenotifier.ui.settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import com.geofencenotifier.data.db.AppDao

@Composable
fun SettingsScreen(dao: AppDao) {
    val settings by dao.getSettings().collectAsState(initial = null)
    Surface { Column { Text("Settings UI. Paused: ${settings?.automationPaused}") } }
}