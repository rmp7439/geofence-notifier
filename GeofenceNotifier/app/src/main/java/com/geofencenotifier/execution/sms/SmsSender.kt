package com.geofencenotifier.execution.sms
import android.telephony.SmsManager
import com.geofencenotifier.data.db.AppDao

class SmsSender(private val dao: AppDao) {
    suspend fun sendSms(jobId: Long, phone: String, message: String): Boolean {
        return try {
            dao.updateSmsJobState(jobId, "SENDING")
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            dao.updateSmsJobState(jobId, "SENT", sentAt = System.currentTimeMillis())
            true
        } catch (e: Exception) {
            dao.updateSmsJobState(jobId, "FAILED", error = e.message ?: "Unknown Error")
            false
        }
    }
}