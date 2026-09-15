package com.geofencenotifier.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geofencenotifier.geofence.GeofenceRegistrar
import com.geofencenotifier.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            val dao = AppDatabase.getInstance(context).dao()
            val registrar = GeofenceRegistrar(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    dao.getActiveLocationsSync().forEach { loc ->
                        registrar.registerGeofence(loc.id.toString(), loc.latitude, loc.longitude, loc.radiusMeters)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}