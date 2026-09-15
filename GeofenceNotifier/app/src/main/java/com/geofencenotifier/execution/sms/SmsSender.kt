package com.geofencenotifier.execution.sms
import android.telephony.SmsManager
import com.geofencenotifier.data.db.AppDao

class SmsSender(private val dao: AppDao) {
    suspend fun sendSms(jobId: Long, phone: String, message: String) {
        dao.updateSmsJobStatus(jobId, "SENDING")
        try {
            SmsManager.getDefault().sendTextMessage(phone, null, message, null, null)
            dao.updateSmsJobStatus(jobId, "SENT")
            dao.updateSmsJobSentAt(jobId, System.currentTimeMillis())
        } catch (e: Exception) {
            dao.updateSmsJobStatus(jobId, "FAILED")
            dao.updateSmsJobError(jobId, e.message ?: "Unknown Error")
        }
    }
}