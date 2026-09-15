package com.geofencenotifier.data.db
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.*

@Dao
interface AppDao {
    @Query("SELECT * FROM Location") fun getLocations(): Flow<List<Location>>
    @Query("SELECT * FROM Location WHERE active = 1") suspend fun getActiveLocationsSync(): List<Location>
    @Insert suspend fun insertLocation(loc: Location): Long

    @Query("SELECT * FROM Recipient") fun getRecipients(): Flow<List<Recipient>>
    @Query("SELECT * FROM Recipient WHERE id = :id") suspend fun getRecipientSync(id: Long): Recipient?
    
    @Query("SELECT * FROM NotificationRule WHERE locationId = :locId") suspend fun getRulesByLocationIdSync(locId: Long): List<NotificationRule>
    
    @Query("SELECT * FROM Event ORDER BY detectedAt DESC") fun getEvents(): Flow<List<Event>>
    @Insert suspend fun insertEvent(evt: Event): Long
    @Query("SELECT * FROM Event WHERE dedupeKey = :key LIMIT 1") suspend fun getEventByDedupeKey(key: String): Event?
    @Query("UPDATE Event SET status = :status WHERE id = :id") suspend fun updateEventStatus(id: Long, status: String)

    @Query("SELECT * FROM AppSettings WHERE id = 1") fun getSettings(): Flow<AppSettings?>
    
    @Insert suspend fun insertSmsJob(job: SmsJob)
    @Query("SELECT * FROM SmsJob WHERE status = 'PENDING'") suspend fun getPendingSmsJobsSync(): List<SmsJob>
    @Query("UPDATE SmsJob SET status = :status WHERE id = :id") suspend fun updateSmsJobStatus(id: Long, status: String)
    @Query("UPDATE SmsJob SET lastError = :error WHERE id = :id") suspend fun updateSmsJobError(id: Long, error: String)
    @Query("UPDATE SmsJob SET sentAt = :time WHERE id = :id") suspend fun updateSmsJobSentAt(id: Long, time: Long)
    
    @Insert suspend fun insertCallJob(job: CallJob)
}