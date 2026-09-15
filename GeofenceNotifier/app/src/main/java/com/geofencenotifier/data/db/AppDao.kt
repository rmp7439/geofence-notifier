package com.geofencenotifier.data.db
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.geofencenotifier.core.model.*

@Dao
interface AppDao {
    @Query("SELECT * FROM Location") fun getLocations(): Flow<List<Location>>
    @Query("SELECT * FROM Location WHERE id = :id") suspend fun getLocationSync(id: Long): Location?
    @Query("SELECT * FROM Location WHERE active = 1") suspend fun getActiveLocationsSync(): List<Location>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertLocation(loc: Location): Long
    @Update suspend fun updateLocation(loc: Location)
    @Delete suspend fun deleteLocation(loc: Location)

    @Query("SELECT * FROM Recipient") fun getRecipients(): Flow<List<Recipient>>
    @Query("SELECT * FROM Recipient WHERE id = :id") suspend fun getRecipientSync(id: Long): Recipient?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRecipient(rec: Recipient): Long
    @Update suspend fun updateRecipient(rec: Recipient)
    @Delete suspend fun deleteRecipient(rec: Recipient)
    
    @Query("SELECT * FROM NotificationRule") fun getRules(): Flow<List<NotificationRule>>
    @Query("SELECT * FROM NotificationRule WHERE locationId = :locId") suspend fun getRulesByLocationIdSync(locId: Long): List<NotificationRule>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRule(rule: NotificationRule): Long
    @Update suspend fun updateRule(rule: NotificationRule)
    @Delete suspend fun deleteRule(rule: NotificationRule)
    
    @Query("SELECT * FROM Event ORDER BY detectedAt DESC") fun getEvents(): Flow<List<Event>>
    @Query("SELECT * FROM Event WHERE status = 'PROCESSING'") suspend fun getProcessingEventsSync(): List<Event>
    @Query("SELECT * FROM Event WHERE dedupeKey = :key LIMIT 1") suspend fun getEventByDedupeKey(key: String): Event?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertEvent(evt: Event): Long
    @Query("UPDATE Event SET status = :status, completedAt = :completedAt WHERE id = :id") suspend fun updateEventStatus(id: Long, status: String, completedAt: Long? = null)

    @Insert suspend fun insertSmsJob(job: SmsJob)
    @Query("SELECT * FROM SmsJob WHERE eventId = :eventId") suspend fun getSmsJobsForEvent(eventId: Long): List<SmsJob>
    @Query("SELECT * FROM SmsJob WHERE id = :id") suspend fun getSmsJobSync(id: Long): SmsJob?
    @Query("SELECT * FROM SmsJob WHERE status IN ('PENDING', 'RETRYING')") suspend fun getPendingSmsJobsSync(): List<SmsJob>
    
    @Query("UPDATE SmsJob SET status = 'SENDING', attemptCount = attemptCount + 1, sendingStartedAt = :startedAt WHERE id = :id AND status IN ('PENDING', 'RETRYING')") suspend fun claimSmsJob(id: Long, startedAt: Long = System.currentTimeMillis()): Int
    @Query("UPDATE SmsJob SET status = :status, lastError = :error, sentAt = :sentAt WHERE id = :id") suspend fun updateSmsJobState(id: Long, status: String, error: String? = null, sentAt: Long? = null)
    @Query("UPDATE SmsJob SET status = 'RETRYING', lastError = 'Stale SMS job recovered' WHERE status = 'SENDING' AND attemptCount < 3 AND sendingStartedAt < :cutoff") suspend fun recoverStaleSmsJobs(cutoff: Long)
    @Query("UPDATE SmsJob SET status = 'FAILED', lastError = 'Stale SMS job failed (max attempts)' WHERE status = 'SENDING' AND attemptCount >= 3 AND sendingStartedAt < :cutoff") suspend fun failStaleSmsJobs(cutoff: Long)

    @Insert suspend fun insertCallJob(job: CallJob)
    @Query("SELECT * FROM CallJob WHERE eventId = :eventId") suspend fun getCallJobsForEvent(eventId: Long): List<CallJob>
    @Query("SELECT * FROM CallJob WHERE status IN ('PENDING', 'RETRYING') ORDER BY sequenceNumber ASC") suspend fun getPendingCallJobsSync(): List<CallJob>
    
    @Query("UPDATE CallJob SET status = 'DIALING', attemptCount = attemptCount + 1, startedAt = :startedAt WHERE id = :id AND status IN ('PENDING', 'RETRYING')") suspend fun claimCallJob(id: Long, startedAt: Long = System.currentTimeMillis()): Int
    @Query("UPDATE CallJob SET status = :status, lastError = :error, endedAt = :endedAt, observedTelephonyState = :state WHERE id = :id") suspend fun updateCallJobState(id: Long, status: String, error: String? = null, endedAt: Long? = null, state: String? = null)
    
    @Query("UPDATE CallJob SET status = 'RETRYING', lastError = 'Stale call recovered' WHERE status = 'DIALING' AND attemptCount < 3 AND startedAt < :cutoff") suspend fun recoverStaleCallJobs(cutoff: Long)
    @Query("UPDATE CallJob SET status = 'FAILED', lastError = 'Stale call failed (max attempts)' WHERE status = 'DIALING' AND attemptCount >= 3 AND startedAt < :cutoff") suspend fun failStaleCallJobs(cutoff: Long)

    @Query("SELECT * FROM AppSettings WHERE id = 1") fun getSettings(): Flow<AppSettings?>
    @Query("SELECT * FROM AppSettings WHERE id = 1") suspend fun getSettingsSync(): AppSettings?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSettings(settings: AppSettings)

    @Query("DELETE FROM Event WHERE detectedAt < :cutoffTime") suspend fun deleteOldEvents(cutoffTime: Long)

    @Transaction
    suspend fun insertEventWithJobs(event: Event, smsJobs: List<SmsJob>, callJobs: List<CallJob>): Long {
        val eventId = insertEvent(event)
        if (eventId == -1L) return -1L
        smsJobs.forEach { insertSmsJob(it.copy(eventId = eventId)) }
        callJobs.forEach { insertCallJob(it.copy(eventId = eventId)) }
        return eventId
    }
}