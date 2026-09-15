package com.geofencenotifier.core.model
import androidx.room.*

@Entity(tableName = "Location")
data class Location(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 150f,
    val triggerType: String = "BOTH",
    val active: Boolean = true,
    val cooldownMinutes: Int = 10,
    val activeHoursStart: String? = null,
    val activeHoursEnd: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "Recipient")
data class Recipient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val smsEnabled: Boolean,
    val callEnabled: Boolean,
    val callCount: Int = 2,
    val callDurationSeconds: Int = 12
)

@Entity(
    tableName = "NotificationRule",
    foreignKeys = [
        ForeignKey(entity = Location::class, parentColumns = ["id"], childColumns = ["locationId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Recipient::class, parentColumns = ["id"], childColumns = ["recipientId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("locationId"), Index("recipientId")]
)
data class NotificationRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val recipientId: Long,
    val messageTemplate: String
)

@Entity(
    tableName = "Event",
    foreignKeys = [ForeignKey(entity = Location::class, parentColumns = ["id"], childColumns = ["locationId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["dedupeKey"], unique = true), Index("locationId")]
)
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val transitionType: String,
    val dedupeKey: String,
    val status: String, // PROCESSING, COMPLETED, FAILED
    val detectedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(
    tableName = "SmsJob",
    foreignKeys = [
        ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Recipient::class, parentColumns = ["id"], childColumns = ["recipientId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventId"), Index("recipientId")]
)
data class SmsJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val recipientId: Long,
    val renderedMessage: String,
    val status: String, // PENDING, SENDING, SENT, RETRYING, FAILED
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val sentAt: Long? = null
)

@Entity(
    tableName = "CallJob",
    foreignKeys = [
        ForeignKey(entity = Event::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Recipient::class, parentColumns = ["id"], childColumns = ["recipientId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("eventId"), Index("recipientId")]
)
data class CallJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val recipientId: Long,
    val sequenceNumber: Int,
    val durationSeconds: Int = 12,
    val status: String, // PENDING, DIALING, ENDED, RETRYING, FAILED
    val observedTelephonyState: String? = null,
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val startedAt: Long? = null,
    val endedAt: Long? = null
)

@Entity(tableName = "AppSettings")
data class AppSettings(
    @PrimaryKey val id: Long = 1,
    val automationPaused: Boolean = false,
    val lastBootRegistrationResult: String? = null,
    val defaultCallDuration: Int = 12,
    val globalCooldownMinutes: Int = 10,
    val logRetentionDays: Int = 30
)