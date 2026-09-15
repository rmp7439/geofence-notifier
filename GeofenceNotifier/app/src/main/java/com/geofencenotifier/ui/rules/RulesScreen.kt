package com.geofencenotifier.ui.rules
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import com.geofencenotifier.data.db.AppDao

@Composable
fun RulesScreen(dao: AppDao) {
    Surface { Column { Text("Rules UI") } }
}