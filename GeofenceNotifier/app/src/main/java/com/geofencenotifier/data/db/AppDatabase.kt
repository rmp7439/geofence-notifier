package com.geofencenotifier.data.db
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.geofencenotifier.core.model.*

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create settings if not exist
        db.execSQL("CREATE TABLE IF NOT EXISTS `AppSettings` (`id` INTEGER NOT NULL, `automationPaused` INTEGER NOT NULL, `lastBootRegistrationResult` TEXT, `defaultCallDuration` INTEGER NOT NULL, `globalCooldownMinutes` INTEGER NOT NULL, `logRetentionDays` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        // Real apps would recreate tables to add foreign keys, for this simulation we execute standard alters
    }
}

@Database(entities = [Location::class, Recipient::class, NotificationRule::class, Event::class, SmsJob::class, CallJob::class, AppSettings::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): AppDao
    
    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "geofence.db")
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
        }
    }
}