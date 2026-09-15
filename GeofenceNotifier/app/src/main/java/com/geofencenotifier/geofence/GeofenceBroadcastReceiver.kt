package com.geofencenotifier.geofence
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("GeofenceReceiver", "Received geofence transition")
        // Pass to EventEngine
    }
}