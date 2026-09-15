package com.geofencenotifier
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.geofencenotifier.ui.theme.GeofenceNotifierTheme
import com.geofencenotifier.ui.dashboard.DashboardScreen
import com.geofencenotifier.data.db.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.getInstance(this).dao()
        setContent {
            GeofenceNotifierTheme {
                Surface {
                    DashboardScreen(dao)
                }
            }
        }
    }
}