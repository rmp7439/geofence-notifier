package com.geofencenotifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.geofencenotifier.ui.dashboard.DashboardScreen
import com.geofencenotifier.ui.locations.LocationsScreen
import com.geofencenotifier.ui.recipients.RecipientsScreen
import com.geofencenotifier.ui.rules.RulesScreen
import com.geofencenotifier.ui.history.HistoryScreen
import com.geofencenotifier.ui.settings.SettingsScreen
import com.geofencenotifier.ui.testmode.TestModeScreen
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.permissions.PermissionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getInstance(this).dao()
        val permManager = PermissionManager(this)
        
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                var permsReady by remember { mutableStateOf(permManager.isReady()) }
                
                if (!permsReady) {
                    PermissionRequestScreen(permManager) { permsReady = true }
                } else {
                    NavHost(navController, startDestination = "dashboard") {
                        composable("dashboard") { DashboardScreen(dao, permManager) { navController.navigate(it) } }
                        composable("locations") { LocationsScreen(dao) { navController.popBackStack() } }
                        composable("recipients") { RecipientsScreen(dao) { navController.popBackStack() } }
                        composable("rules") { RulesScreen(dao) { navController.popBackStack() } }
                        composable("history") { HistoryScreen(dao) { navController.popBackStack() } }
                        composable("settings") { SettingsScreen(dao) { navController.popBackStack() } }
                        composable("testmode") { TestModeScreen(dao) { navController.popBackStack() } }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(manager: PermissionManager, onReady: () -> Unit) {
    Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            Text("Permissions Required", style = MaterialTheme.typography.headlineMedium)
            Text("Geofence Notifier requires permissions to function.")
            // Staged permissions request would go here in a real production app.
            Button(onClick = { onReady() }) { Text("Acknowledge (Simulated)") }
        }
    }
}