package com.geofencenotifier.ui.rules
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.NotificationRule
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(dao: AppDao, onBack: () -> Unit) {
    val rules by dao.getRules().collectAsState(initial = emptyList())
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val recs by dao.getRecipients().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showDialog = true }) { Text("Add Rule") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rules", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(rules) { rule ->
                    val lName = locs.find { it.id == rule.locationId }?.name ?: "Unknown Loc"
                    val rName = recs.find { it.id == rule.recipientId }?.name ?: "Unknown Recipient"
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row {
                                Text("When: $lName -> Notify: $rName", modifier = Modifier.weight(1f))
                                Switch(checked = rule.enabled, onCheckedChange = { 
                                    scope.launch { dao.updateRule(rule.copy(enabled = it)) } 
                                })
                            }
                            Text(rule.messageTemplate, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { scope.launch { dao.deleteRule(rule) } }) { Text("Del") }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showDialog) {
        var selectedLoc by remember { mutableStateOf<Long?>(null) }
        var selectedRec by remember { mutableStateOf<Long?>(null) }
        var msg by remember { mutableStateOf("Alert! {location} triggered {event} at {time}.") }
        var expandedLoc by remember { mutableStateOf(false) }
        var expandedRec by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Rule") },
            text = {
                Column {
                    ExposedDropdownMenuBox(expanded = expandedLoc, onExpandedChange = { expandedLoc = !expandedLoc }) {
                        val selName = locs.find { it.id == selectedLoc }?.name ?: "Select Location"
                        OutlinedTextField(value = selName, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor())
                        ExposedDropdownMenu(expanded = expandedLoc, onDismissRequest = { expandedLoc = false }) {
                            locs.forEach { l ->
                                DropdownMenuItem(text = { Text(l.name) }, onClick = { selectedLoc = l.id; expandedLoc = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = expandedRec, onExpandedChange = { expandedRec = !expandedRec }) {
                        val selName = recs.find { it.id == selectedRec }?.name ?: "Select Recipient"
                        OutlinedTextField(value = selName, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor())
                        ExposedDropdownMenu(expanded = expandedRec, onDismissRequest = { expandedRec = false }) {
                            recs.forEach { r ->
                                DropdownMenuItem(text = { Text(r.name) }, onClick = { selectedRec = r.id; expandedRec = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Message Template") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (selectedLoc != null && selectedRec != null && msg.isNotBlank()) {
                        scope.launch {
                            dao.insertRule(NotificationRule(locationId = selectedLoc!!, recipientId = selectedRec!!, messageTemplate = msg, enabled = true))
                            showDialog = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}