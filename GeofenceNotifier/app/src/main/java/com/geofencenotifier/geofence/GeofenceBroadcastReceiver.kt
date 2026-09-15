package com.geofencenotifier.geofence
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import com.geofencenotifier.engine.EventEngine
import com.geofencenotifier.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return
        
        val transition = event.geofenceTransition.toString()
        val triggeringGeofences = event.triggeringGeofences ?: return
        
        val pendingResult = goAsync()
        val dao = AppDatabase.getInstance(context).dao()
        val engine = EventEngine(dao)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                triggeringGeofences.forEach { geofence ->
                    val locId = geofence.requestId.toLongOrNull() ?: return@forEach
                    engine.processTransition(locId, transition)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}