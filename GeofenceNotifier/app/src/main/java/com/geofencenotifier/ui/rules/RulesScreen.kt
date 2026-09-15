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
    var editingRule by remember { mutableStateOf<NotificationRule?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showAdd = true }) { Text("Add Rule") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Notification Rules", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(rules) { rule ->
                    val locName = locs.find { it.id == rule.locationId }?.name ?: "Unknown Location"
                    val recName = recs.find { it.id == rule.recipientId }?.name ?: "Unknown Recipient"
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("When at: $locName", style = MaterialTheme.typography.titleMedium)
                                    Text("Notify: $recName")
                                    Text("Template: ${rule.messageTemplate}", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(checked = rule.enabled, onCheckedChange = { en ->
                                    scope.launch { dao.updateRule(rule.copy(enabled = en)) }
                                })
                            }
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { editingRule = rule }) { Text("Edit") }
                                IconButton(onClick = { scope.launch { dao.deleteRule(rule) } }) { Text("Del") }
                            }
                        }
                    }
                }
            }
        }
    }
    
    val currentEditor = editingRule
    if (showAdd || currentEditor != null) {
        var locId by remember { mutableStateOf(currentEditor?.locationId ?: locs.firstOrNull()?.id ?: 0L) }
        var recId by remember { mutableStateOf(currentEditor?.recipientId ?: recs.firstOrNull()?.id ?: 0L) }
        var template by remember { mutableStateOf(currentEditor?.messageTemplate ?: "Alert: {location} {event} at {time}") }
        var enabled by remember { mutableStateOf(currentEditor?.enabled ?: true) }
        var errorMsg by remember { mutableStateOf("") }
        var locExpanded by remember { mutableStateOf(false) }
        var recExpanded by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showAdd = false; editingRule = null },
            title = { Text(if (currentEditor == null) "Add Rule" else "Edit Rule") },
            text = {
                Column {
                    ExposedDropdownMenuBox(expanded = locExpanded, onExpandedChange = { locExpanded = !locExpanded }) {
                        val selLoc = locs.find { it.id == locId }?.name ?: "Select Location"
                        OutlinedTextField(value = selLoc, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(), label = { Text("Location") })
                        ExposedDropdownMenu(expanded = locExpanded, onDismissRequest = { locExpanded = false }) {
                            locs.forEach { l ->
                                DropdownMenuItem(text = { Text(l.name) }, onClick = { locId = l.id; locExpanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = recExpanded, onExpandedChange = { recExpanded = !recExpanded }) {
                        val selRec = recs.find { it.id == recId }?.name ?: "Select Recipient"
                        OutlinedTextField(value = selRec, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(), label = { Text("Recipient") })
                        ExposedDropdownMenu(expanded = recExpanded, onDismissRequest = { recExpanded = false }) {
                            recs.forEach { r ->
                                DropdownMenuItem(text = { Text(r.name) }, onClick = { recId = r.id; recExpanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = template, onValueChange = { template = it }, label = { Text("Message Template") }, modifier = Modifier.fillMaxWidth())
                    Text("{location}, {event}, {time} will be replaced", style = MaterialTheme.typography.bodySmall)
                    
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("Enabled"); Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    if (errorMsg.isNotBlank()) Text(errorMsg, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (locId == 0L || recId == 0L) {
                        errorMsg = "Must select location and recipient"
                    } else if (template.isBlank()) {
                        errorMsg = "Template cannot be blank"
                    } else {
                        val r = NotificationRule(
                            id = currentEditor?.id ?: 0,
                            locationId = locId,
                            recipientId = recId,
                            messageTemplate = template,
                            enabled = enabled
                        )
                        scope.launch {
                            if (currentEditor == null) dao.insertRule(r)
                            else dao.updateRule(r)
                            showAdd = false
                            editingRule = null
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showAdd = false; editingRule = null }) { Text("Cancel") } }
        )
    }
}