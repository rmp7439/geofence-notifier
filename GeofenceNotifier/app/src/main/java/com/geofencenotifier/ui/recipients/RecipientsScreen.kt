package com.geofencenotifier.ui.recipients
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.Recipient
import kotlinx.coroutines.launch

@Composable
fun RecipientsScreen(dao: AppDao, onBack: () -> Unit) {
    val recs by dao.getRecipients().collectAsState(initial = emptyList())
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
            Text("Recipients", style = MaterialTheme.typography.headlineMedium)
            
            LazyColumn {
                items(recs) { rec ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(rec.name, style = MaterialTheme.typography.titleMedium)
                                Text(rec.phoneNumber)
                                Text("SMS: ${rec.smsEnabled} | Call: ${rec.callEnabled}")
                            }
                            IconButton(onClick = { scope.launch { dao.deleteRecipient(rec) } }) {
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
        var phone by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
                            dao.insertRecipient(Recipient(name = name, phoneNumber = phone, smsEnabled = true, callEnabled = true))
                            showDialog = false
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}