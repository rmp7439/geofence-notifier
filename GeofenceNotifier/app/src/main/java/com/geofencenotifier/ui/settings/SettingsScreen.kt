package com.geofencenotifier.ui.settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.AppSettings
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(dao: AppDao, onBack: () -> Unit) {
    val settings by dao.getSettings().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            settings?.let { s ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Pause Automation")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = s.automationPaused,
                        onCheckedChange = { scope.launch { dao.insertSettings(s.copy(automationPaused = it)) } }
                    )
                }
                Text("Global Cooldown (mins): ${s.globalCooldownMinutes}")
                Text("Default Call Duration (secs): ${s.defaultCallDuration}")
                Text("Log Retention (days): ${s.logRetentionDays}")
            } ?: Text("Loading settings...")
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Samsung Battery Guidance:", style = MaterialTheme.typography.titleMedium)
            Text("To ensure reliable geofence triggers, please set this app to 'Unrestricted' battery usage in Android Settings. Force-stops will kill geofence listeners.")
        }
    }
}