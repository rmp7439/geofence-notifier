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
        val smsSender = SmsSender(dao)
        val callExecutor = CallExecutor(applicationContext, dao)
        
        var requiresRetry = false
        
        // SMS Execution
        val pendingSms = dao.getPendingSmsJobsSync()
        for (job in pendingSms) {
            if (job.attemptCount >= 3) {
                dao.updateSmsJobState(job.id, "FAILED", error = "Max retries exceeded")
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
        
        for ((eventId, calls) in eventsToCalls) {
            // Sequence calls: execute the first pending one in sequence
            val callToExecute = calls.minByOrNull { it.sequenceNumber } ?: continue
            
            if (callToExecute.attemptCount >= 3) {
                dao.updateCallJobState(callToExecute.id, "FAILED", error = "Max retries exceeded")
                continue
            }
            
            val rec = dao.getRecipientSync(callToExecute.recipientId)
            if (rec != null) {
                val success = callExecutor.executeFixedDurationCall(callToExecute.id, rec.phoneNumber, callToExecute.durationSeconds)
                if (!success) requiresRetry = true
            } else {
                dao.updateCallJobState(callToExecute.id, "FAILED", error = "Recipient missing")
            }
        }
        
        // Finalize Events
        val processingEvents = dao.getProcessingEventsSync()
        for (evt in processingEvents) {
            val sms = dao.getSmsJobsForEvent(evt.id)
            val calls = dao.getCallJobsForEvent(evt.id)
            val allSmsDone = sms.all { it.status == "SENT" || it.status == "FAILED" }
            val allCallsDone = calls.all { it.status == "ENDED" || it.status == "FAILED" }
            if (allSmsDone && allCallsDone) {
                dao.updateEventStatus(evt.id, "COMPLETE", System.currentTimeMillis())
            }
        }
        
        return if (requiresRetry) Result.retry() else Result.success()
    }
}