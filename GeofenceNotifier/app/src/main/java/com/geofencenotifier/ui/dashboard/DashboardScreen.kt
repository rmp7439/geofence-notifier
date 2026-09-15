package com.geofencenotifier.ui.dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import com.geofencenotifier.data.db.AppDao
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geofencenotifier.permissions.PermissionManager

@Composable
fun DashboardScreen(dao: AppDao, permManager: PermissionManager, onNavigate: (String) -> Unit) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val recs by dao.getRecipients().collectAsState(initial = emptyList())
    val rules by dao.getRules().collectAsState(initial = emptyList())
    val settings by dao.getSettings().collectAsState(initial = null)
    
    val activeLocs = locs.count { it.active }
    val registeredLocs = locs.count { it.registrationState == "REGISTERED" }
    val enabledRules = rules.count { it.enabled }
    
    val isReady = permManager.isReady() && 
                  settings?.automationPaused != true && 
                  activeLocs > 0 && 
                  registeredLocs == activeLocs &&
                  enabledRules > 0 &&
                  recs.isNotEmpty()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Geofence Notifier", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("System Readiness", style = MaterialTheme.typography.titleMedium)
                    
                    if (isReady) {
                        Text("READY & RUNNING", color = MaterialTheme.colorScheme.primary)
                    } else {
                        Text("NOT READY", color = MaterialTheme.colorScheme.error)
                        if (!permManager.isReady()) Text("- MISSING PERMISSIONS", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (settings?.automationPaused == true) Text("- AUTOMATION PAUSED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (activeLocs == 0) Text("- NO ACTIVE LOCATIONS", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (registeredLocs < activeLocs) Text("- GEOFENCE REGISTRATION FAILED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (enabledRules == 0) Text("- NO ENABLED RULES", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        if (recs.isEmpty()) Text("- NO RECIPIENTS", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Text("Active Locations: $activeLocs / ${locs.size}")
                    Text("Recipients: ${recs.size}")
                    Text("Active Rules: $enabledRules / ${rules.size}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Boot Status: ${settings?.lastBootRegistrationResult ?: "Not yet run"}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigate("locations") }, modifier = Modifier.fillMaxWidth()) { Text("Locations") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigate("recipients") }, modifier = Modifier.fillMaxWidth()) { Text("Recipients") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigate("rules") }, modifier = Modifier.fillMaxWidth()) { Text("Rules") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigate("history") }, modifier = Modifier.fillMaxWidth()) { Text("History") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigate("settings") }, modifier = Modifier.fillMaxWidth()) { Text("Settings") }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigate("testmode") }, modifier = Modifier.fillMaxWidth()) { Text("Safe Test Mode") }
        }
    }
}