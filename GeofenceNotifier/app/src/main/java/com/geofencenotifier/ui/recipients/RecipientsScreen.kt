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
    var editingRec by remember { mutableStateOf<Recipient?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { showAdd = true }) { Text("Add") }
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
                            Text("Calls: ${rec.callCount} @ ${rec.callDurationSeconds}s", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = { editingRec = rec }) { Text("Edit") }
                                IconButton(onClick = { scope.launch { dao.deleteRecipient(rec) } }) { Text("Del") }
                            }
                        }
                    }
                }
            }
        }
    }
    
    val currentEditor = editingRec
    if (showAdd || currentEditor != null) {
        var name by remember { mutableStateOf(currentEditor?.name ?: "") }
        var phone by remember { mutableStateOf(currentEditor?.phoneNumber ?: "") }
        var smsEn by remember { mutableStateOf(currentEditor?.smsEnabled ?: true) }
        var callEn by remember { mutableStateOf(currentEditor?.callEnabled ?: false) }
        var callCount by remember { mutableStateOf((currentEditor?.callCount ?: 2).toString()) }
        var callDur by remember { mutableStateOf((currentEditor?.callDurationSeconds ?: 12).toString()) }
        var errorMsg by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAdd = false; editingRec = null },
            title = { Text(if (currentEditor == null) "Add Recipient" else "Edit Recipient") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                    Row {
                        Text("SMS"); Switch(checked = smsEn, onCheckedChange = { smsEn = it })
                        Spacer(Modifier.width(8.dp))
                        Text("Call"); Switch(checked = callEn, onCheckedChange = { callEn = it })
                    }
                    if (callEn) {
                        OutlinedTextField(value = callCount, onValueChange = { callCount = it }, label = { Text("Call Count") })
                        OutlinedTextField(value = callDur, onValueChange = { callDur = it }, label = { Text("Call Duration (s)") })
                    }
                    if (errorMsg.isNotBlank()) Text(errorMsg, color = MaterialTheme.colorScheme.error)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val cCount = callCount.toIntOrNull()
                    val cDur = callDur.toIntOrNull()
                    if (name.isBlank() || phone.isBlank()) {
                        errorMsg = "Fields cannot be blank"
                    } else if (callEn && (cCount == null || cCount < 1 || cDur == null || cDur < 1)) {
                        errorMsg = "Invalid call count/duration"
                    } else {
                        val finalRec = Recipient(
                            id = currentEditor?.id ?: 0,
                            name = name,
                            phoneNumber = phone,
                            smsEnabled = smsEn,
                            callEnabled = callEn,
                            callCount = cCount ?: 2,
                            callDurationSeconds = cDur ?: 12
                        )
                        scope.launch {
                            if (currentEditor == null) dao.insertRecipient(finalRec)
                            else dao.updateRecipient(finalRec)
                            showAdd = false
                            editingRec = null
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showAdd = false; editingRec = null }) { Text("Cancel") } }
        )
    }
}