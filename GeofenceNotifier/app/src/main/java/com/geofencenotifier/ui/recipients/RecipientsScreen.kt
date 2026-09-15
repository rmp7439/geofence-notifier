package com.geofencenotifier.ui.recipients
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Recipient
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecipientsScreen(dao: AppDao, onBack: () -> Unit) {
    val recs by dao.getRecipients().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showAdd = true }) { Text("Add Recipient") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Recipients", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(recs) { rec ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(rec.name, style = MaterialTheme.typography.titleMedium)
                            Text("Phone: ${rec.phoneNumber}")
                            Row {
                                Text("SMS", modifier = Modifier.padding(end = 8.dp))
                                Switch(checked = rec.smsEnabled, onCheckedChange = { scope.launch { dao.updateRecipient(rec.copy(smsEnabled = it)) } })
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Call", modifier = Modifier.padding(end = 8.dp))
                                Switch(checked = rec.callEnabled, onCheckedChange = { scope.launch { dao.updateRecipient(rec.copy(callEnabled = it)) } })
                            }
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { scope.launch { dao.deleteRecipient(rec) } }) { Text("Del") }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add Recipient") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        scope.launch {
                            dao.insertRecipient(Recipient(name = name, phoneNumber = phone, smsEnabled = true, callEnabled = false))
                            showAdd = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }
}