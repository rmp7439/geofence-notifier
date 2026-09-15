package com.geofencenotifier.execution.sms
import android.telephony.SmsManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.geofencenotifier.data.db.AppDao

class SmsSender(private val dao: AppDao, private val context: Context) {
    suspend fun sendSms(jobId: Long, phone: String, message: String): Boolean {
        return try {
            val sentIntent = PendingIntent.getBroadcast(
                context,
                jobId.toInt(),
                Intent("SMS_SENT"),
                PendingIntent.FLAG_IMMUTABLE
            )
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, sentIntent, null)
            // We assume it's SENT to network for now. A real PendingIntent receiver could update to DELIVERED.
            dao.updateSmsJobState(jobId, "SENT", sentAt = System.currentTimeMillis())
            true
        } catch (e: Exception) {
            dao.updateSmsJobState(jobId, "RETRYING", error = e.message ?: "Unknown SMS Error")
            false
        }
    }
}