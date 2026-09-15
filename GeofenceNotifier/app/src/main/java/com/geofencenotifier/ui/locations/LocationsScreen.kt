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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(dao: AppDao, onBack: () -> Unit) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editingLoc by remember { mutableStateOf<Location?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showAdd = true }) { Text("Add") }
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
                                    Text("Cooldown: ${loc.cooldownMinutes}m", style = MaterialTheme.typography.bodySmall)
                                    if (loc.activeHoursStart != null && loc.activeHoursEnd != null) {
                                        Text("Active: ${loc.activeHoursStart} to ${loc.activeHoursEnd}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("State: ${loc.registrationState}", color = if (loc.registrationState == "REGISTRATION_FAILED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(checked = loc.active, onCheckedChange = { active ->
                                    scope.launch { 
                                        val reg = GeofenceRegistrar(context, dao)
                                        if (active) {
                                            dao.updateLocation(loc.copy(active = true))
                                            val success = reg.registerGeofence(loc.id)
                                            val finalState = if (success) "REGISTERED" else "REGISTRATION_FAILED"
                                            dao.updateLocation(loc.copy(active = true, registrationState = finalState))
                                            if (!success) snackbarHostState.showSnackbar("Failed to register geofence for ${loc.name}")
                                        } else {
                                            reg.unregisterGeofence(loc.id)
                                            dao.updateLocation(loc.copy(active = false, registrationState = "NOT_REGISTERED"))
                                        }
                                    }
                                })
                            }
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { editingLoc = loc }) { Text("Edit") }
                                IconButton(onClick = { 
                                    scope.launch { 
                                        GeofenceRegistrar(context, dao).unregisterGeofence(loc.id)
                                        dao.deleteLocation(loc)
                                    } 
                                }) { Text("Del") }
                            }
                        }
                    }
                }
            }
        }
        
        val currentEditor = editingLoc
        if (showAdd || currentEditor != null) {
            var name by remember { mutableStateOf(currentEditor?.name ?: "") }
            var latStr by remember { mutableStateOf((currentEditor?.latitude ?: "").toString()) }
            var lngStr by remember { mutableStateOf((currentEditor?.longitude ?: "").toString()) }
            var radStr by remember { mutableStateOf((currentEditor?.radiusMeters ?: 150f).toString()) }
            var trigger by remember { mutableStateOf(currentEditor?.triggerType ?: "BOTH") }
            var cooldown by remember { mutableStateOf((currentEditor?.cooldownMinutes ?: 10).toString()) }
            var startHr by remember { mutableStateOf(currentEditor?.activeHoursStart ?: "") }
            var endHr by remember { mutableStateOf(currentEditor?.activeHoursEnd ?: "") }
            var error by remember { mutableStateOf("") }
            var expandedTrigger by remember { mutableStateOf(false) }
            
            AlertDialog(
                onDismissRequest = { showAdd = false; editingLoc = null },
                title = { Text(if (currentEditor == null) "Add Location" else "Edit Location") },
                text = {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                        Row {
                            OutlinedTextField(value = latStr, onValueChange = { latStr = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(value = lngStr, onValueChange = { lngStr = it }, label = { Text("Lng") }, modifier = Modifier.weight(1f))
                        }
                        OutlinedTextField(value = radStr, onValueChange = { radStr = it }, label = { Text("Radius (m)") })
                        OutlinedTextField(value = cooldown, onValueChange = { cooldown = it }, label = { Text("Cooldown (mins)") })
                        
                        ExposedDropdownMenuBox(expanded = expandedTrigger, onExpandedChange = { expandedTrigger = !expandedTrigger }) {
                            OutlinedTextField(value = trigger, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(), label = { Text("Trigger Type") })
                            ExposedDropdownMenu(expanded = expandedTrigger, onDismissRequest = { expandedTrigger = false }) {
                                listOf("BOTH", "ENTER", "EXIT").forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { trigger = t; expandedTrigger = false })
                                }
                            }
                        }
                        
                        Text("Active Hours (HH:MM) Optional:", style = MaterialTheme.typography.labelSmall)
                        Row {
                            OutlinedTextField(value = startHr, onValueChange = { startHr = it }, label = { Text("Start") }, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(value = endHr, onValueChange = { endHr = it }, label = { Text("End") }, modifier = Modifier.weight(1f))
                        }
                        
                        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val lat = latStr.toDoubleOrNull()
                        val lng = lngStr.toDoubleOrNull()
                        val rad = radStr.toFloatOrNull()
                        val cd = cooldown.toIntOrNull()
                        
                        val hrRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")
                        val sHrValid = startHr.isBlank() || hrRegex.matches(startHr)
                        val eHrValid = endHr.isBlank() || hrRegex.matches(endHr)
                        val bothHr = (startHr.isNotBlank() && endHr.isNotBlank()) || (startHr.isBlank() && endHr.isBlank())
                        
                        if (name.isBlank() || lat == null || lng == null || rad == null || cd == null || rad < 100f || lat !in -90.0..90.0 || lng !in -180.0..180.0 || cd < 0) {
                            error = "Invalid basic config. Rad >= 100. CD >= 0."
                        } else if (!sHrValid || !eHrValid || !bothHr) {
                            error = "Active hours must be HH:MM or both blank."
                        } else {
                            scope.launch {
                                val reg = GeofenceRegistrar(context, dao)
                                if (currentEditor != null) {
                                    reg.unregisterGeofence(currentEditor.id)
                                    val finalLoc = Location(
                                        id = currentEditor.id,
                                        name = name,
                                        latitude = lat,
                                        longitude = lng,
                                        radiusMeters = rad,
                                        triggerType = trigger,
                                        active = currentEditor.active,
                                        cooldownMinutes = cd,
                                        activeHoursStart = startHr.takeIf { it.isNotBlank() },
                                        activeHoursEnd = endHr.takeIf { it.isNotBlank() },
                                        registrationState = "NOT_REGISTERED"
                                    )
                                    dao.updateLocation(finalLoc)
                                    if (finalLoc.active) {
                                        val success = reg.registerGeofence(finalLoc.id)
                                        val finalState = if (success) "REGISTERED" else "REGISTRATION_FAILED"
                                        dao.updateLocation(finalLoc.copy(registrationState = finalState))
                                        if (!success) snackbarHostState.showSnackbar("Failed to re-register geofence for ${finalLoc.name}")
                                    }
                                } else {
                                    val initialLoc = Location(
                                        id = 0,
                                        name = name,
                                        latitude = lat,
                                        longitude = lng,
                                        radiusMeters = rad,
                                        triggerType = trigger,
                                        active = true,
                                        cooldownMinutes = cd,
                                        activeHoursStart = startHr.takeIf { it.isNotBlank() },
                                        activeHoursEnd = endHr.takeIf { it.isNotBlank() },
                                        registrationState = "NOT_REGISTERED"
                                    )
                                    val id = dao.insertLocation(initialLoc)
                                    val success = reg.registerGeofence(id)
                                    val finalState = if (success) "REGISTERED" else "REGISTRATION_FAILED"
                                    dao.updateLocation(initialLoc.copy(id = id, registrationState = finalState))
                                    if (!success) snackbarHostState.showSnackbar("Failed to register geofence for ${initialLoc.name}")
                                }
                                showAdd = false
                                editingLoc = null
                            }
                        }
                    }) { Text("Save") }
                },
                dismissButton = { Button(onClick = { showAdd = false; editingLoc = null }) { Text("Cancel") } }
            )
        }
    }
}