package com.geofencenotifier.data.db
import androidx.room.*
import android.content.Context
import com.geofencenotifier.core.model.*

@Database(entities = [Location::class, Recipient::class, NotificationRule::class, Event::class, SmsJob::class, CallJob::class, AppSettings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
    
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "geofence.db").build().also { INSTANCE = it }
        }
    }
}