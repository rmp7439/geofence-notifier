package com.geofencenotifier.ui.locations
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Location
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.geofencenotifier.geofence.GeofenceRegistrar

@Composable
fun LocationsScreen(dao: AppDao, onBack: () -> Unit) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showAdd = true }) { Text("Add Location") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Locations", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(locs) { loc ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(loc.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Lat: ${loc.latitude} | Lng: ${loc.longitude}")
                                    Text("Radius: ${loc.radiusMeters}m | Trigger: ${loc.triggerType}")
                                }
                                Switch(checked = loc.active, onCheckedChange = { active ->
                                    scope.launch { 
                                        dao.updateLocation(loc.copy(active = active))
                                        val reg = GeofenceRegistrar(context, dao)
                                        if (active) reg.registerGeofence(loc.id) else reg.unregisterGeofence(loc.id)
                                    }
                                })
                            }
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { 
                                    scope.launch { 
                                        dao.deleteLocation(loc)
                                        GeofenceRegistrar(context, dao).unregisterGeofence(loc.id)
                                    } 
                                }) { Text("Del") }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var latStr by remember { mutableStateOf("") }
        var lngStr by remember { mutableStateOf("") }
        var radStr by remember { mutableStateOf("150") }
        var error by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Location") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = latStr, onValueChange = { latStr = it }, label = { Text("Latitude") })
                    OutlinedTextField(value = lngStr, onValueChange = { lngStr = it }, label = { Text("Longitude") })
                    OutlinedTextField(value = radStr, onValueChange = { radStr = it }, label = { Text("Radius (meters)") })
                    if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val lat = latStr.toDoubleOrNull()
                    val lng = lngStr.toDoubleOrNull()
                    val rad = radStr.toFloatOrNull()
                    if (name.isBlank() || lat == null || lng == null || rad == null || rad < 100f || lat !in -90.0..90.0 || lng !in -180.0..180.0) {
                        error = "Invalid inputs. Radius must be >= 100m. Lat/Lng must be valid."
                    } else {
                        scope.launch {
                            val id = dao.insertLocation(Location(name = name, latitude = lat, longitude = lng, radiusMeters = rad))
                            GeofenceRegistrar(context, dao).registerGeofence(id)
                            showAdd = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}