package com.geofencenotifier.ui.testmode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.engine.EventEngine
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestModeScreen(dao: AppDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedLoc by remember { mutableStateOf<Long?>(null) }
    var transition by remember { mutableStateOf("ENTER") }
    val locs by dao.getLocations().collectAsState(initial = emptyList())
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("SAFE TEST MODE", style = MaterialTheme.typography.headlineMedium)
            Text("Simulated events will route through standard engine constraints.")
            Spacer(modifier = Modifier.height(16.dp))
            
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                val selName = locs.find { it.id == selectedLoc }?.name ?: "Select Location"
                OutlinedTextField(value = selName, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor())
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    locs.forEach { l ->
                        DropdownMenuItem(text = { Text(l.name) }, onClick = { selectedLoc = l.id; expanded = false })
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                RadioButton(selected = transition == "ENTER", onClick = { transition = "ENTER" })
                Text("ENTER", modifier = Modifier.padding(end = 16.dp, top = 12.dp))
                RadioButton(selected = transition == "EXIT", onClick = { transition = "EXIT" })
                Text("EXIT", modifier = Modifier.padding(top = 12.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (selectedLoc != null) {
                    scope.launch {
                        val engine = EventEngine(context, dao)
                        engine.processTransition(selectedLoc!!, transition)
                    }
                }
            }) {
                Text("Simulate Transition")
            }
        }
    }
}