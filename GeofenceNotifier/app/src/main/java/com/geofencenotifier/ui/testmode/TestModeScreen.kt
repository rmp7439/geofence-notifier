package com.geofencenotifier.ui.testmode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.geofencenotifier.data.db.AppDao
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geofencenotifier.core.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestModeScreen(dao: AppDao, onBack: () -> Unit) {
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var selectedLoc by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Safe Test Mode", style = MaterialTheme.typography.headlineMedium)
            Text("Events triggered here DO NOT touch SMS or Telecom APIs.", style = MaterialTheme.typography.bodySmall)
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            if (resultText.isNotBlank()) {
                Text(resultText, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Function that does exactly what EventEngine does, but flags the jobs as completed immediately
// so that real execution never happens, or uses mock recipient IDs.
suspend fun runIsolatedTest(dao: AppDao, locationId: Long, transition: String) {
    val dedupeKey = "TEST_${locationId}_${transition}_${System.currentTimeMillis()}"
    val event = Event(locationId = locationId, transitionType = transition, dedupeKey = dedupeKey, status = "COMPLETED")
    val eventId = dao.insertEventWithJobs(event, emptyList(), emptyList())
}