package com.geofencenotifier
import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.core.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.geofencenotifier.workers.OutboxRetryWorker

class GeofenceNotifierApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(this@GeofenceNotifierApp)
            val dao = db.dao()
            
            // Safe initialization of default settings if none exist
            val currentSettings = dao.getSettingsSync()
            if (currentSettings == null) {
                dao.insertSettings(AppSettings())
            }
            
            // Schedule periodic maintenance worker (runs OutboxRetryWorker periodically as a catch-all)
            val maintenanceRequest = PeriodicWorkRequestBuilder<OutboxRetryWorker>(1, TimeUnit.HOURS)
                .build()
                
            WorkManager.getInstance(this@GeofenceNotifierApp).enqueueUniquePeriodicWork(
                "MaintenanceOutbox",
                ExistingPeriodicWorkPolicy.KEEP,
                maintenanceRequest
            )
        }
    }
}