package com.geofencenotifier.workers
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.execution.sms.SmsSender
import com.geofencenotifier.execution.call.CallExecutor

class OutboxRetryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = AppDatabase.getInstance(applicationContext).dao()
        val smsSender = SmsSender(dao, applicationContext)
        val callExecutor = CallExecutor(applicationContext, dao)
        
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
        
        // Call Execution
        val pendingCalls = dao.getPendingCallJobsSync()
        val eventsToCalls = pendingCalls.groupBy { it.eventId }
        
        for ((_, calls) in eventsToCalls) {
            val callToExecute = calls.minByOrNull { it.sequenceNumber } ?: continue
            
            if (callToExecute.attemptCount >= 3) {
                dao.updateCallJobState(callToExecute.id, "FAILED", error = "Max retries exceeded")
                // Continuing to next sequence since this one is terminal
                hasPendingWork = true // Next sequence will be picked up on next run
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
                    hasPendingWork = true // More calls in sequence
                }
            } else {
                dao.updateCallJobState(callToExecute.id, "FAILED", error = "Recipient missing")
                hasPendingWork = true // Process next sequence
            }
        }
        
        // Finalize Events
        val processingEvents = dao.getProcessingEventsSync()
        for (evt in processingEvents) {
            val sms = dao.getSmsJobsForEvent(evt.id)
            val calls = dao.getCallJobsForEvent(evt.id)
            val allSmsTerminal = sms.all { it.status == "SENT" || it.status == "FAILED" }
            val allCallsTerminal = calls.all { it.status == "ENDED" || it.status == "FAILED" }
            
            if (allSmsTerminal && allCallsTerminal) {
                val hasFailures = sms.any { it.status == "FAILED" } || calls.any { it.status == "FAILED" }
                val allFailed = (sms.isNotEmpty() || calls.isNotEmpty()) && sms.all { it.status == "FAILED" } && calls.all { it.status == "FAILED" }
                
                val terminalState = if (allFailed) "FAILED" else "COMPLETED"
                dao.updateEventStatus(evt.id, terminalState, System.currentTimeMillis())
            } else if (sms.isEmpty() && calls.isEmpty()) {
                dao.updateEventStatus(evt.id, "COMPLETED", System.currentTimeMillis())
            }
        }
        
        // Retention cleanup
        val settings = dao.getSettingsSync()
        if (settings != null) {
            val cutoff = System.currentTimeMillis() - (settings.logRetentionDays * 24L * 60 * 60 * 1000)
            dao.deleteOldEvents(cutoff)
        }
        
        return if (requiresRetry) Result.retry() else if (hasPendingWork) Result.retry() else Result.success()
    }
}