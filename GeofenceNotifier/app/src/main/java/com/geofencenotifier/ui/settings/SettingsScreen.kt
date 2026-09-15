package com.geofencenotifier.ui.settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import com.geofencenotifier.data.db.AppDao
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(dao: AppDao, onBack: () -> Unit) {
    val settingsFlow = dao.getSettings().collectAsState(initial = null)
    val settings = settingsFlow.value
    val scope = rememberCoroutineScope()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
            
            if (settings != null) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Automation Paused", modifier = Modifier.weight(1f))
                    Switch(checked = settings.automationPaused, onCheckedChange = { 
                        scope.launch { dao.insertSettings(settings.copy(automationPaused = it)) } 
                    })
                }
                
                var cooldown by remember { mutableStateOf(settings.globalCooldownMinutes.toString()) }
                OutlinedTextField(
                    value = cooldown, 
                    onValueChange = { cooldown = it; it.toIntOrNull()?.let { v -> scope.launch { dao.insertSettings(settings.copy(globalCooldownMinutes = v)) } } },
                    label = { Text("Global Cooldown (mins)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                var callDur by remember { mutableStateOf(settings.defaultCallDuration.toString()) }
                OutlinedTextField(
                    value = callDur, 
                    onValueChange = { callDur = it; it.toIntOrNull()?.let { v -> scope.launch { dao.insertSettings(settings.copy(defaultCallDuration = v)) } } },
                    label = { Text("Default Call Duration (sec)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                var ret by remember { mutableStateOf(settings.logRetentionDays.toString()) }
                OutlinedTextField(
                    value = ret, 
                    onValueChange = { ret = it; it.toIntOrNull()?.let { v -> scope.launch { dao.insertSettings(settings.copy(logRetentionDays = v)) } } },
                    label = { Text("Log Retention (days)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("System Guidance:", style = MaterialTheme.typography.titleMedium)
                Text("- Set Battery to 'Unrestricted' for reliable geofencing.", style = MaterialTheme.typography.bodySmall)
                Text("- Force-stopping this app will kill geofences until reopened.", style = MaterialTheme.typography.bodySmall)
                Text("- Boot Status: ${settings.lastBootRegistrationResult ?: "None"}", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Loading settings...")
            }
        }
    }
}