package com.geofencenotifier.data.db
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.*

@Dao
interface AppDao {
    // Locations
    @Query("SELECT * FROM Location") fun getLocations(): Flow<List<Location>>
    @Query("SELECT * FROM Location WHERE id = :id") suspend fun getLocationSync(id: Long): Location?
    @Query("SELECT * FROM Location WHERE active = 1") suspend fun getActiveLocationsSync(): List<Location>
    @Insert suspend fun insertLocation(loc: Location): Long
    @Update suspend fun updateLocation(loc: Location)
    @Delete suspend fun deleteLocation(loc: Location)

    // Recipients
    @Query("SELECT * FROM Recipient") fun getRecipients(): Flow<List<Recipient>>
    @Query("SELECT * FROM Recipient WHERE id = :id") suspend fun getRecipientSync(id: Long): Recipient?
    @Insert suspend fun insertRecipient(rec: Recipient): Long
    @Update suspend fun updateRecipient(rec: Recipient)
    @Delete suspend fun deleteRecipient(rec: Recipient)
    
    // Rules
    @Query("SELECT * FROM NotificationRule") fun getRules(): Flow<List<NotificationRule>>
    @Query("SELECT * FROM NotificationRule WHERE locationId = :locId") suspend fun getRulesByLocationIdSync(locId: Long): List<NotificationRule>
    @Insert suspend fun insertRule(rule: NotificationRule): Long
    @Update suspend fun updateRule(rule: NotificationRule)
    @Delete suspend fun deleteRule(rule: NotificationRule)
    
    // Events
    @Query("SELECT * FROM Event ORDER BY detectedAt DESC") fun getEvents(): Flow<List<Event>>
    @Query("SELECT * FROM Event WHERE status = 'PROCESSING'") suspend fun getProcessingEventsSync(): List<Event>
    @Query("SELECT * FROM Event WHERE dedupeKey = :key LIMIT 1") suspend fun getEventByDedupeKey(key: String): Event?
    @Insert suspend fun insertEvent(evt: Event): Long
    @Query("UPDATE Event SET status = :status, completedAt = :completedAt WHERE id = :id") suspend fun updateEventStatus(id: Long, status: String, completedAt: Long? = null)

    // SmsJobs
    @Insert suspend fun insertSmsJob(job: SmsJob)
    @Query("SELECT * FROM SmsJob WHERE eventId = :eventId") suspend fun getSmsJobsForEvent(eventId: Long): List<SmsJob>
    @Query("SELECT * FROM SmsJob WHERE status IN ('PENDING', 'RETRYING')") suspend fun getPendingSmsJobsSync(): List<SmsJob>
    @Query("UPDATE SmsJob SET status = :status, attemptCount = attemptCount + 1, lastError = :error, sentAt = :sentAt WHERE id = :id") suspend fun updateSmsJobState(id: Long, status: String, error: String? = null, sentAt: Long? = null)
    
    // CallJobs
    @Insert suspend fun insertCallJob(job: CallJob)
    @Query("SELECT * FROM CallJob WHERE eventId = :eventId") suspend fun getCallJobsForEvent(eventId: Long): List<CallJob>
    @Query("SELECT * FROM CallJob WHERE status IN ('PENDING', 'RETRYING') ORDER BY sequenceNumber ASC") suspend fun getPendingCallJobsSync(): List<CallJob>
    @Query("UPDATE CallJob SET status = :status, attemptCount = attemptCount + 1, lastError = :error, startedAt = :startedAt, endedAt = :endedAt, observedTelephonyState = :state WHERE id = :id") suspend fun updateCallJobState(id: Long, status: String, error: String? = null, startedAt: Long? = null, endedAt: Long? = null, state: String? = null)

    // Settings
    @Query("SELECT * FROM AppSettings WHERE id = 1") fun getSettings(): Flow<AppSettings?>
    @Query("SELECT * FROM AppSettings WHERE id = 1") suspend fun getSettingsSync(): AppSettings?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSettings(settings: AppSettings)
    
    // Transactions
    @Transaction
    suspend fun insertEventWithJobs(event: Event, smsJobs: List<SmsJob>, callJobs: List<CallJob>) {
        val eventId = insertEvent(event)
        smsJobs.forEach { insertSmsJob(it.copy(eventId = eventId)) }
        callJobs.forEach { insertCallJob(it.copy(eventId = eventId)) }
    }
}