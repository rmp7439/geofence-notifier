package com.geofencenotifier.data.db
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.*

@Dao
interface AppDao {
    @Query("SELECT * FROM Location") fun getLocations(): Flow<List<Location>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLocation(loc: Location): Long
    @Delete suspend fun deleteLocation(loc: Location)

    @Query("SELECT * FROM Recipient") fun getRecipients(): Flow<List<Recipient>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRecipient(rec: Recipient): Long
    @Delete suspend fun deleteRecipient(rec: Recipient)

    @Query("SELECT * FROM NotificationRule") fun getRules(): Flow<List<NotificationRule>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRule(rule: NotificationRule): Long
    @Delete suspend fun deleteRule(rule: NotificationRule)

    @Query("SELECT * FROM Event ORDER BY detectedAt DESC") fun getEvents(): Flow<List<Event>>
    @Insert suspend fun insertEvent(evt: Event): Long

    @Query("SELECT * FROM AppSettings WHERE id = 1") fun getSettings(): Flow<AppSettings?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSettings(settings: AppSettings)
    
    @Query("SELECT * FROM Event WHERE dedupeKey = :key LIMIT 1") suspend fun getEventByDedupeKey(key: String): Event?
    
    @Insert suspend fun insertSmsJob(job: SmsJob)
    @Insert suspend fun insertCallJob(job: CallJob)
}