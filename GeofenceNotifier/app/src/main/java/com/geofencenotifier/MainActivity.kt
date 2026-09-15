package com.geofencenotifier
import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.geofencenotifier.ui.dashboard.DashboardScreen
import com.geofencenotifier.ui.locations.LocationsScreen
import com.geofencenotifier.ui.recipients.RecipientsScreen
import com.geofencenotifier.ui.rules.RulesScreen
import com.geofencenotifier.ui.history.HistoryScreen
import com.geofencenotifier.ui.settings.SettingsScreen
import com.geofencenotifier.ui.testmode.TestModeScreen
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.permissions.PermissionManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                    PermissionRequestScreen(permManager) { permsReady = permManager.isReady() }
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
fun PermissionRequestScreen(manager: PermissionManager, onCheck: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    
    val baseLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        onCheck()
        step++
    }
    
    val bgLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        onCheck()
        step++
    }
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Permissions Required", style = MaterialTheme.typography.headlineMedium)
            
            if (!manager.hasForegroundLocation() || !manager.hasSms() || !manager.hasCall() || !manager.hasAnswerCall() || !manager.hasNotifications()) {
                Text("We need base permissions (Location, SMS, Call).")
                Button(onClick = {
                    val reqs = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) reqs.add(Manifest.permission.ANSWER_PHONE_CALLS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) reqs.add(Manifest.permission.POST_NOTIFICATIONS)
                    baseLauncher.launch(reqs.toTypedArray())
                }) { Text("Request Base Permissions") }
            } else if (!manager.hasBackgroundLocation()) {
                Text("We need background location to trigger geofences.")
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    } else {
                        onCheck()
                    }
                }) { Text("Request Background Location") }
            } else {
                Text("All required permissions seem granted or you denied permanently. Go to app settings if it's stuck.")
                Button(onClick = onCheck) { Text("Re-Check") }
            }
        }
    }
}