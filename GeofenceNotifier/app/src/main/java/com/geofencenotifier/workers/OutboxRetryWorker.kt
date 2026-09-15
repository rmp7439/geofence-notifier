package com.geofencenotifier.workers
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geofencenotifier.data.db.AppDatabase
import com.geofencenotifier.execution.sms.SmsSender

class OutboxRetryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = AppDatabase.getInstance(applicationContext).dao()
        val smsSender = SmsSender(dao)
        val pendingSms = dao.getPendingSmsJobsSync()
        
        var hasFailures = false
        for (job in pendingSms) {
            val rec = dao.getRecipientSync(job.recipientId)
            if (rec != null) {
                try {
                    smsSender.sendSms(job.id, rec.phoneNumber, job.renderedMessage)
                } catch (e: Exception) {
                    hasFailures = true
                }
            }
        }
        return if (hasFailures) Result.retry() else Result.success()
    }
}