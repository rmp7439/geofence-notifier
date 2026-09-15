package com.geofencenotifier.ui.testmode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import com.geofencenotifier.data.db.AppDao

@Composable
fun TestModeScreen(dao: AppDao) {
    Surface { Column { Text("Test Mode Settings") } }
}