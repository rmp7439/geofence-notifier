package com.geofencenotifier.geofence
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.geofencenotifier.data.db.AppDao
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.util.Log

class GeofenceRegistrar(private val context: Context, private val dao: AppDao) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private fun getPendingIntent(locationId: Long): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            putExtra("LOCATION_ID", locationId)
        }
        return PendingIntent.getBroadcast(
            context,
            locationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    suspend fun registerGeofence(locationId: Long): Boolean {
        val loc = dao.getLocationSync(locationId) ?: return false
        if (!loc.active) return false
        
        val transitionTypes = when (loc.triggerType) {
            "ENTER" -> Geofence.GEOFENCE_TRANSITION_ENTER
            "EXIT" -> Geofence.GEOFENCE_TRANSITION_EXIT
            else -> Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        }
        
        val geofence = Geofence.Builder()
            .setRequestId(loc.id.toString())
            .setCircularRegion(loc.latitude, loc.longitude, loc.radiusMeters.takeIf { it >= 100f } ?: 100f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionTypes)
            .build()
            
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
            
        return suspendCoroutine { cont ->
            try {
                geofencingClient.addGeofences(request, getPendingIntent(loc.id))
                    .addOnSuccessListener { cont.resume(true) }
                    .addOnFailureListener { e -> 
                        Log.e("GeofenceRegistrar", "Failed to register geofence", e)
                        cont.resume(false) 
                    }
            } catch (e: SecurityException) {
                Log.e("GeofenceRegistrar", "Missing permission", e)
                cont.resume(false)
            }
        }
    }
    
    suspend fun unregisterGeofence(locationId: Long) {
        suspendCoroutine<Unit> { cont ->
            try {
                geofencingClient.removeGeofences(getPendingIntent(locationId))
                    .addOnCompleteListener { cont.resume(Unit) }
            } catch (e: Exception) {
                Log.e("GeofenceRegistrar", "Failed to unregister", e)
                cont.resume(Unit) // Best effort
            }
        }
    }
    
    suspend fun registerAllActiveGeofences(): String {
        val activeLocations = dao.getActiveLocationsSync()
        if (activeLocations.isEmpty()) return "NO_ACTIVE_LOCATIONS"
        
        var successCount = 0
        for (loc in activeLocations) {
            val res = registerGeofence(loc.id)
            if (res) successCount++
        }
        
        return when {
            successCount == activeLocations.size -> "SUCCESS"
            successCount > 0 -> "PARTIAL_FAILURE ($successCount/${activeLocations.size})"
            else -> "FAILURE"
        }
    }
}