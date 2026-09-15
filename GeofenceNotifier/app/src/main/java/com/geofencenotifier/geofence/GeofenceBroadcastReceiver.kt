package com.geofencenotifier.geofence
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.geofencenotifier.engine.EventEngine
import com.geofencenotifier.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            pendingResult.finish()
            return
        }
        
        val locationId = intent.getLongExtra("LOCATION_ID", -1L)
        if (locationId == -1L) {
            pendingResult.finish()
            return
        }
        
        val transition = when (geofencingEvent.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).dao()
                val engine = EventEngine(context, dao)
                engine.processTransition(locationId, transition)
            } finally {
                pendingResult.finish()
            }
        }
    }
}