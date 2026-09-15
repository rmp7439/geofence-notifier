package com.geofencenotifier.ui.rules
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.NotificationRule
import kotlinx.coroutines.launch

@Composable
fun RulesScreen(dao: AppDao, onBack: () -> Unit) {
    val rules by dao.getRules().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showDialog = true }) { Text("Add") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rules", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(rules) { rule ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rule for Loc: ${rule.locationId} -> Recip: ${rule.recipientId}")
                                Text(rule.messageTemplate, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { scope.launch { dao.deleteRule(rule) } }) {
                                Text("Del")
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showDialog) {
        var locId by remember { mutableStateOf("") }
        var recId by remember { mutableStateOf("") }
        var msg by remember { mutableStateOf("Alert! {location} triggered.") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Rule") },
            text = {
                Column {
                    OutlinedTextField(value = locId, onValueChange = { locId = it }, label = { Text("Location ID") })
                    OutlinedTextField(value = recId, onValueChange = { recId = it }, label = { Text("Recipient ID") })
                    OutlinedTextField(value = msg, onValueChange = { msg = it }, label = { Text("Message Template") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    val l = locId.toLongOrNull()
                    val r = recId.toLongOrNull()
                    if (l != null && r != null && msg.isNotBlank()) {
                        scope.launch {
                            dao.insertRule(NotificationRule(locationId = l, recipientId = r, messageTemplate = msg))
                            showDialog = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}