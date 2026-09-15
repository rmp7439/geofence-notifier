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
                
                var cooldown by remember(settings.globalCooldownMinutes) { mutableStateOf(settings.globalCooldownMinutes.toString()) }
                var cdError by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = cooldown, 
                    onValueChange = { 
                        cooldown = it
                        val v = it.toIntOrNull()
                        if (v != null && v >= 0) {
                            cdError = false
                            scope.launch { dao.insertSettings(settings.copy(globalCooldownMinutes = v)) }
                        } else {
                            cdError = true
                        }
                    },
                    label = { Text("Global Cooldown (mins)") },
                    isError = cdError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (cdError) Text("Must be >= 0", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                
                var callDur by remember(settings.defaultCallDuration) { mutableStateOf(settings.defaultCallDuration.toString()) }
                var callDurError by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = callDur, 
                    onValueChange = { 
                        callDur = it
                        val v = it.toIntOrNull()
                        if (v != null && v in 1..300) {
                            callDurError = false
                            scope.launch { dao.insertSettings(settings.copy(defaultCallDuration = v)) }
                        } else {
                            callDurError = true
                        }
                    },
                    label = { Text("Default Call Duration (sec)") },
                    isError = callDurError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (callDurError) Text("Must be 1-300", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                
                var ret by remember(settings.logRetentionDays) { mutableStateOf(settings.logRetentionDays.toString()) }
                var retError by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = ret, 
                    onValueChange = { 
                        ret = it
                        val v = it.toIntOrNull()
                        if (v != null && v in 1..365) {
                            retError = false
                            scope.launch { dao.insertSettings(settings.copy(logRetentionDays = v)) }
                        } else {
                            retError = true
                        }
                    },
                    label = { Text("Log Retention (days)") },
                    isError = retError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (retError) Text("Must be 1-365", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                
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