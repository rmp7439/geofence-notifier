package com.geofencenotifier.ui.testmode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.engine.EventEngine
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@Composable
fun TestModeScreen(dao: AppDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var locId by remember { mutableStateOf("") }
    var transition by remember { mutableStateOf("ENTER") }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onBack) { Text("Back") }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Test Mode (Simulated Event)", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(value = locId, onValueChange = { locId = it }, label = { Text("Location ID to Test") })
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                RadioButton(selected = transition == "ENTER", onClick = { transition = "ENTER" })
                Text("ENTER", modifier = Modifier.padding(end = 16.dp, top = 12.dp))
                RadioButton(selected = transition == "EXIT", onClick = { transition = "EXIT" })
                Text("EXIT", modifier = Modifier.padding(top = 12.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                val lId = locId.toLongOrNull()
                if (lId != null) {
                    scope.launch {
                        val engine = EventEngine(context, dao)
                        engine.processTransition(lId, transition)
                    }
                }
            }) {
                Text("Simulate Transition")
            }
        }
    }
}