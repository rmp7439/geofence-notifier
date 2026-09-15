package com.geofencenotifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getInstance(this).dao()
        setContent {
            GeofenceNotifierTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") { DashboardScreen(dao) }
                    composable("locations") { LocationsScreen(dao) }
                    composable("recipients") { RecipientsScreen(dao) }
                    composable("rules") { RulesScreen(dao) }
                    composable("settings") { SettingsScreen(dao) }
                    composable("testmode") { TestModeScreen(dao) }
                    composable("history") { HistoryScreen(dao) }
                }
            }
        }
    }
}