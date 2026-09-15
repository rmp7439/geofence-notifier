
package com.geofencenotifier.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geofencenotifier.core.model.*

@Database(
    entities = [
        Location::class,
        Recipient::class,
        NotificationRule::class,
        Event::class,
        SmsJob::class,
        CallJob::class,
        AppSettings::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    // DAOs would go here
}
