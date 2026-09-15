package com.geofencenotifier.ui.rules
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao

@Composable
fun RulesScreen(dao: AppDao) {
    Surface {
        LazyColumn {
            item { Text("Rule Management Configuration") }
        }
    }
}