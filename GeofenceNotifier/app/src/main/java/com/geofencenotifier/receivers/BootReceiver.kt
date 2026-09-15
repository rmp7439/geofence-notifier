package com.geofencenotifier.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.geofence.GeofenceRegistrar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = AppDatabase.getInstance(context).dao()
                    val registrar = GeofenceRegistrar(context, dao)
                    val result = registrar.registerAllActiveGeofences()
                    val settings = dao.getSettingsSync()
                    if (settings != null) {
                        dao.insertSettings(settings.copy(lastBootRegistrationResult = result))
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}