
package com.geofencenotifier.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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

@Entity(tableName = "NotificationRule")
data class NotificationRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val recipientId: Long,
    val messageTemplate: String
)

@Entity(tableName = "Event")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val transitionType: String,
    val dedupeKey: String,
    val status: String,
    val detectedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(tableName = "SmsJob")
data class SmsJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val recipientId: Long,
    val renderedMessage: String,
    val status: String,
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val sentAt: Long? = null
)

@Entity(tableName = "CallJob")
data class CallJob(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: Long,
    val recipientId: Long,
    val sequenceNumber: Int,
    val status: String,
    val observedTelephonyState: String? = null,
    val startedAt: Long? = null,
    val endedAt: Long? = null
)

@Entity(tableName = "AppSettings")
data class AppSettings(
    @PrimaryKey val id: Long = 1,
    val automationPaused: Boolean = false,
    val lastBootRegistrationResult: String? = null,
    val logRetentionDays: Int = 30
)
