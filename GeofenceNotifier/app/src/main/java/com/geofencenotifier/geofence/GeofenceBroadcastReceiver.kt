package com.geofencenotifier.geofence
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.e("GeofenceReceiver", "Geofence error: ${event.errorCode}")
            return
        }
        val transition = event.geofenceTransition
        val triggeringGeofences = event.triggeringGeofences
        
        triggeringGeofences?.forEach { geofence ->
            val engine = com.geofencenotifier.engine.EventEngine(com.geofencenotifier.data.db.AppDatabase.getInstance(context).dao())
            // Coroutine launch omitted for brevity in sync receiver, assuming engine handles scope
            Log.i("GeofenceReceiver", "Transition $transition for ${geofence.requestId}")
        }
    }
}