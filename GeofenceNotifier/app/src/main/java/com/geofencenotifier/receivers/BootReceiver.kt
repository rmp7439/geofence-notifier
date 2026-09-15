package com.geofencenotifier.receivers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.geofence.GeofenceRegistrar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = AppDatabase.getInstance(context).dao()
                    
                    val hasBackgroundLoc = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                    } else {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    }
                    
                    if (!hasBackgroundLoc) {
                        val settings = dao.getSettingsSync()
                        if (settings != null) dao.insertSettings(settings.copy(lastBootRegistrationResult = "FAILED: Missing Permissions"))
                        return@launch
                    }
                    
                    val registrar = GeofenceRegistrar(context, dao)
                    val result = registrar.registerAllActiveGeofences()
                    val settings = dao.getSettingsSync()
                    if (settings != null) {
                        dao.insertSettings(settings.copy(lastBootRegistrationResult = result))
                    }
                } catch (e: Exception) {
                    val dao = AppDatabase.getInstance(context).dao()
                    val settings = dao.getSettingsSync()
                    if (settings != null) {
                        dao.insertSettings(settings.copy(lastBootRegistrationResult = "FAILED: ${e.message}"))
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}