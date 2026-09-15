package com.geofencenotifier.data.db
import androidx.room.*
import com.geofencenotifier.core.model.*

@Database(entities = [Location::class, Recipient::class, NotificationRule::class, Event::class, SmsJob::class, CallJob::class, AppSettings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
}