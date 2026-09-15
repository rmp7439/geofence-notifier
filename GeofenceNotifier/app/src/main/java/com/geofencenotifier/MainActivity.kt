package com.geofencenotifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.geofencenotifier.ui.theme.GeofenceNotifierTheme
import com.geofencenotifier.ui.dashboard.DashboardScreen
import com.geofencenotifier.ui.locations.LocationsScreen
import com.geofencenotifier.ui.recipients.RecipientsScreen
import com.geofencenotifier.ui.rules.RulesScreen
import com.geofencenotifier.ui.settings.SettingsScreen
import com.geofencenotifier.ui.testmode.TestModeScreen
import com.geofencenotifier.ui.history.HistoryScreen
import com.geofencenotifier.data.db.AppDatabase
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.geofencenotifier.core.model.AppSettings
import com.geofencenotifier.permissions.PermissionManager
import android.Manifest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getInstance(this).dao()
        
        lifecycleScope.launch {
            if (dao.getSettingsSync() == null) {
                dao.insertSettings(AppSettings())
            }
        }
        
        val permManager = PermissionManager(this)
        if (!permManager.isReady()) {
            val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            requestPermissions(perms.toTypedArray(), 100)
        }
        
        setContent {
            GeofenceNotifierTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") { DashboardScreen(dao, onNavigate = { dest -> navController.navigate(dest) }) }
                    composable("locations") { LocationsScreen(dao, onBack = { navController.popBackStack() }) }
                    composable("recipients") { RecipientsScreen(dao, onBack = { navController.popBackStack() }) }
                    composable("rules") { RulesScreen(dao, onBack = { navController.popBackStack() }) }
                    composable("settings") { SettingsScreen(dao, onBack = { navController.popBackStack() }) }
                    composable("testmode") { TestModeScreen(dao, onBack = { navController.popBackStack() }) }
                    composable("history") { HistoryScreen(dao, onBack = { navController.popBackStack() }) }
                }
            }
        }
    }
}