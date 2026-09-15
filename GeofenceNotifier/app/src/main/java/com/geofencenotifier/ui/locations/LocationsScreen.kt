package com.geofencenotifier.ui.locations
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Location
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.geofencenotifier.geofence.GeofenceRegistrar

@Composable
fun LocationsScreen(dao: AppDao, onBack: () -> Unit) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showDialog = true }) { Text("Add") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Locations", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(locs) { loc ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(loc.name, style = MaterialTheme.typography.titleMedium)
                                Text("${loc.latitude}, ${loc.longitude} - Radius: ${loc.radiusMeters}m")
                            }
                            Switch(
                                checked = loc.active,
                                onCheckedChange = { active -> 
                                    scope.launch { 
                                        val updated = loc.copy(active = active)
                                        dao.updateLocation(updated)
                                        val registrar = GeofenceRegistrar(context, dao)
                                        if (active) registrar.registerGeofence(updated.id) else registrar.unregisterGeofence(updated.id)
                                    } 
                                }
                            )
                            IconButton(onClick = { scope.launch { 
                                dao.deleteLocation(loc)
                                GeofenceRegistrar(context, dao).unregisterGeofence(loc.id)
                            } }) {
                                Text("Del")
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var lat by remember { mutableStateOf("") }
        var lng by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Location") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Latitude") })
                    OutlinedTextField(value = lng, onValueChange = { lng = it }, label = { Text("Longitude") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val l = lat.toDoubleOrNull()
                    val g = lng.toDoubleOrNull()
                    if (name.isNotBlank() && l != null && g != null) {
                        scope.launch {
                            val id = dao.insertLocation(Location(name = name, latitude = l, longitude = g))
                            GeofenceRegistrar(context, dao).registerGeofence(id)
                            showDialog = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}