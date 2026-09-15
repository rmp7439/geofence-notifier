package com.geofencenotifier.execution.sms
import android.telephony.SmsManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.geofencenotifier.data.db.AppDao

class SmsSender(private val dao: AppDao, private val context: Context) {
    suspend fun sendSms(jobId: Long, phone: String, message: String): Boolean {
        return try {
            val intent = Intent(context, SmsSentReceiver::class.java).apply {
                putExtra("JOB_ID", jobId)
            }
            val sentIntent = PendingIntent.getBroadcast(
                context,
                jobId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, sentIntent, null)
            true
        } catch (e: Exception) {
            val state = "RETRYING"
            dao.updateSmsJobState(jobId, state, error = e.message ?: "Unknown SMS Error")
            false
        }
    }
}