package com.geofencenotifier.engine
import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.BackoffPolicy
import com.geofencenotifier.data.db.AppDao
import com.geofencenotifier.core.model.*
import com.geofencenotifier.workers.OutboxRetryWorker
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventEngine(private val context: Context, private val dao: AppDao) {
    suspend fun processTransition(locationId: Long, transition: String) {
        val location = dao.getLocationSync(locationId) ?: return
        if (!location.active) return

        if (location.triggerType != "BOTH" && location.triggerType != transition) return

        val settings = dao.getSettings().firstOrNull()
        if (settings?.automationPaused == true) return

        if (location.activeHoursStart != null && location.activeHoursEnd != null) {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)
            val currentMinutes = hour * 60 + min

            val startParts = location.activeHoursStart.split(":")
            val endParts = location.activeHoursEnd.split(":")
            if (startParts.size == 2 && endParts.size == 2) {
                val sH = startParts[0].toIntOrNull()
                val sM = startParts[1].toIntOrNull()
                val eH = endParts[0].toIntOrNull()
                val eM = endParts[1].toIntOrNull()

                if (sH != null && sM != null && eH != null && eM != null) {
                    val startMins = sH * 60 + sM
                    val endMins = eH * 60 + eM

                    val inWindow = if (startMins <= endMins) {
                        currentMinutes in startMins..endMins
                    } else {
                        currentMinutes >= startMins || currentMinutes <= endMins
                    }
                    if (!inWindow) return
                } else {
                    return
                }
            } else {
                return
            }
        }

        // Ensure cooldown is strictly >= 1
        var cooldownMins = location.cooldownMinutes.takeIf { it > 0 } ?: settings?.globalCooldownMinutes ?: 10
        if (cooldownMins < 1) cooldownMins = 10

        val cooldownMillis = cooldownMins * 60000L
        val timeWindow = System.currentTimeMillis() / cooldownMillis
        val dedupeKey = "${locationId}_${transition}_$timeWindow"

        val event = Event(locationId = locationId, transitionType = transition, dedupeKey = dedupeKey, status = "PROCESSING")
        val rules = dao.getRulesByLocationIdSync(locationId).filter { it.enabled }

        val smsJobs = mutableListOf<SmsJob>()
        val callJobs = mutableListOf<CallJob>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timeStr = sdf.format(Date())

        rules.forEach { rule ->
            val recipient = dao.getRecipientSync(rule.recipientId) ?: return@forEach
            val msg = rule.messageTemplate
                .replace("{location}", location.name)
                .replace("{event}", transition)
                .replace("{time}", timeStr)

            if (recipient.smsEnabled) {
                smsJobs.add(SmsJob(eventId = 0, recipientId = recipient.id, renderedMessage = msg, status = "PENDING"))
            }
            if (recipient.callEnabled) {
                val callDuration = recipient.callDurationSeconds.takeIf { it > 0 } ?: settings?.defaultCallDuration ?: 12
                for (i in 1..recipient.callCount) {
                    callJobs.add(CallJob(eventId = 0, recipientId = recipient.id, sequenceNumber = i, durationSeconds = callDuration, status = "PENDING"))
                }
            }
        }

        val eventId = dao.insertEventWithJobs(event, smsJobs, callJobs)
        if (eventId != -1L) {
            val req = OneTimeWorkRequestBuilder<OutboxRetryWorker>()
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }
}
