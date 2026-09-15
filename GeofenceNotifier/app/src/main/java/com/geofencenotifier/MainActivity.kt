package com.geofencenotifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.geofencenotifier.ui.theme.GeofenceNotifierTheme
import com.geofencenotifier.ui.dashboard.DashboardScreen
import com.geofencenotifier.ui.locations.LocationsScreen
import com.geofencenotifier.ui.recipients.RecipientsScreen
import com.geofencenotifier.ui.rules.RulesScreen
import com.geofencenotifier.ui.history.HistoryScreen
import com.geofencenotifier.ui.settings.SettingsScreen
import com.geofencenotifier.ui.testmode.TestModeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeofenceNotifierTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") { DashboardScreen(navController) }
                        composable("locations") { LocationsScreen() }
                        composable("recipients") { RecipientsScreen() }
                        composable("rules") { RulesScreen() }
                        composable("history") { HistoryScreen() }
                        composable("settings") { SettingsScreen() }
                        composable("testmode") { TestModeScreen() }
                    }
                }
            }
        }
    }
}
