package com.geofencenotifier.geofence
import android.content.Context
import android.util.Log

class GeofenceRegistrar(private val context: Context) {
    fun registerGeofences() {
        Log.i("GeofenceRegistrar", "Registering geofences with Google Play Services")
    }
}