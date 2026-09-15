package com.geofencenotifier.workers
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.execution.sms.SmsSender
import com.geofencenotifier.execution.call.CallExecutor
import kotlinx.coroutines.delay

class OutboxRetryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = AppDatabase.getInstance(applicationContext).dao()
        val smsSender = SmsSender(dao, applicationContext)
        val callExecutor = CallExecutor(applicationContext, dao)
        
        // Stale Job Recovery
        dao.recoverStaleSmsJobs()
        dao.failStaleSmsJobs()
        val cutoff = System.currentTimeMillis() - 60000L // 1 minute stale cutoff for calls
        dao.recoverStaleCallJobs(cutoff)
        dao.failStaleCallJobs(cutoff)
        
        var requiresRetry = false
        var hasPendingWork = false
        
        // SMS Execution
        val pendingSms = dao.getPendingSmsJobsSync()
        for (job in pendingSms) {
            if (job.attemptCount >= 3) {
                dao.updateSmsJobState(job.id, "FAILED", error = "Max retries exceeded")
                continue
            }
            val claimed = dao.claimSmsJob(job.id)
            if (claimed == 0) {
                hasPendingWork = true
                continue
            }
            
            val rec = dao.getRecipientSync(job.recipientId)
            if (rec != null) {
                val success = smsSender.sendSms(job.id, rec.phoneNumber, job.renderedMessage)
                if (!success) requiresRetry = true
            } else {
                dao.updateSmsJobState(job.id, "FAILED", error = "Recipient missing")
            }
        }
        
        // Call Execution (Strict sequencing)
        val pendingCalls = dao.getPendingCallJobsSync()
        val eventsToCalls = pendingCalls.groupBy { it.eventId }
        
        for ((_, calls) in eventsToCalls) {
            val callToExecute = calls.minByOrNull { it.sequenceNumber } ?: continue
            
            if (callToExecute.attemptCount >= 3) {
                dao.updateCallJobState(callToExecute.id, "FAILED", error = "Max retries exceeded")
                if (calls.size > 1) hasPendingWork = true
                continue
            }
            
            val claimed = dao.claimCallJob(callToExecute.id)
            if (claimed == 0) {
                hasPendingWork = true
                continue
            }
            
            val rec = dao.getRecipientSync(callToExecute.recipientId)
            if (rec != null) {
                val success = callExecutor.executeFixedDurationCall(callToExecute.id, rec.phoneNumber, callToExecute.durationSeconds)
                if (!success) {
                    requiresRetry = true
                } else if (calls.size > 1) {
                    hasPendingWork = true
                }
            } else {
                dao.updateCallJobState(callToExecute.id, "FAILED", error = "Recipient missing")
                if (calls.size > 1) hasPendingWork = true
            }
        }
        
        // Allow time for SMS PendingIntents to broadcast before finalizing
        delay(1000)
        
        // Finalize Events
        val processingEvents = dao.getProcessingEventsSync()
        for (evt in processingEvents) {
            val sms = dao.getSmsJobsForEvent(evt.id)
            val calls = dao.getCallJobsForEvent(evt.id)
            val allSmsTerminal = sms.all { it.status == "SENT" || it.status == "FAILED" }
            val allCallsTerminal = calls.all { it.status == "ENDED" || it.status == "FAILED" }
            
            if (allSmsTerminal && allCallsTerminal) {
                val hasFailures = sms.any { it.status == "FAILED" } || calls.any { it.status == "FAILED" }
                val terminalState = if (hasFailures) "FAILED" else "COMPLETED"
                dao.updateEventStatus(evt.id, terminalState, System.currentTimeMillis())
            } else if (sms.isEmpty() && calls.isEmpty()) {
                dao.updateEventStatus(evt.id, "COMPLETED", System.currentTimeMillis())
            } else {
                hasPendingWork = true
            }
        }
        
        // Retention cleanup
        val settings = dao.getSettingsSync()
        if (settings != null) {
            val cutoffDate = System.currentTimeMillis() - (settings.logRetentionDays * 24L * 60 * 60 * 1000)
            dao.deleteOldEvents(cutoffDate)
        }
        
        return if (requiresRetry || hasPendingWork) Result.retry() else Result.success()
    }
}