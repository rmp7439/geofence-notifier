package com.geofencenotifier.geofence
import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceRegistrar(private val context: Context) {
    private val client = LocationServices.getGeofencingClient(context)
    
    fun registerGeofence(id: String, lat: Double, lng: Double, radius: Float) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat, lng, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
            
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
            
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        
        try {
            client.addGeofences(request, pendingIntent)
                .addOnSuccessListener { Log.i("GeofenceRegistrar", "Successfully added geofence $id") }
                .addOnFailureListener { Log.e("GeofenceRegistrar", "Failed to add geofence $id", it) }
        } catch(e: SecurityException) {
            Log.e("GeofenceRegistrar", "Missing permissions", e)
        }
    }
}