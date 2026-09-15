package com.geofencenotifier.ui.testmode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import com.geofencenotifier.data.db.AppDao
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geofencenotifier.core.model.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.BackoffPolicy
import java.util.concurrent.TimeUnit
import com.geofencenotifier.workers.OutboxRetryWorker
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestModeScreen(dao: AppDao, onBack: () -> Unit) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedLoc by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var realSmsPhone by remember { mutableStateOf("") }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Safe Test Mode", style = MaterialTheme.typography.headlineMedium)
            Text("Simulation events DO NOT touch SMS/Telecom APIs.", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                val selName = locs.find { it.id == selectedLoc }?.name ?: "Select Location"
                OutlinedTextField(value = selName, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    locs.forEach { l ->
                        DropdownMenuItem(text = { Text(l.name) }, onClick = { selectedLoc = l.id; expanded = false })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = { 
                    scope.launch { 
                        if (selectedLoc != null) {
                            runIsolatedTest(dao, selectedLoc!!, "ENTER")
                            resultText = "Isolated ENTER test injected into database successfully."
                        }
                    } 
                }) { Text("Simulate ENTER") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { 
                    scope.launch { 
                        if (selectedLoc != null) {
                            runIsolatedTest(dao, selectedLoc!!, "EXIT")
                            resultText = "Isolated EXIT test injected into database successfully."
                        }
                    } 
                }) { Text("Simulate EXIT") }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("DANGEROUS: REAL SMS TEST", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Text("Requires explicit user action. Uses isolated test recipient. NEVER triggers calls.", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(value = realSmsPhone, onValueChange = { realSmsPhone = it }, label = { Text("Test Phone Number") })
            
            Button(
                onClick = {
                    if (realSmsPhone.isNotBlank() && selectedLoc != null) {
                        scope.launch {
                            val recId = dao.insertRecipient(Recipient(name = "TEST_SMS_RECIPIENT", phoneNumber = realSmsPhone, smsEnabled = true, callEnabled = false))
                            val dedupeKey = "TEST_REAL_SMS_${System.currentTimeMillis()}"
                            val event = Event(locationId = selectedLoc!!, transitionType = "TEST", dedupeKey = dedupeKey, status = "PROCESSING")
                            val smsJobs = listOf(SmsJob(eventId = 0, recipientId = recId, renderedMessage = "Test message from Geofence Notifier", status = "PENDING"))
                            
                            val eventId = dao.insertEventWithJobs(event, smsJobs, emptyList())
                            if (eventId != -1L) {
                                val req = OneTimeWorkRequestBuilder<OutboxRetryWorker>()
                                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                                    .build()
                                WorkManager.getInstance(context).enqueue(req)
                                resultText = "REAL SMS Queued."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Send REAL SMS to this number") }
            
            Spacer(modifier = Modifier.height(16.dp))
            if (resultText.isNotBlank()) {
                Text(resultText, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

suspend fun runIsolatedTest(dao: AppDao, locationId: Long, transition: String) {
    val dedupeKey = "TEST_${locationId}_${transition}_${System.currentTimeMillis()}"
    val event = Event(locationId = locationId, transitionType = transition, dedupeKey = dedupeKey, status = "COMPLETED")
    dao.insertEventWithJobs(event, emptyList(), emptyList())
}