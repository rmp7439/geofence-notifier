package com.geofencenotifier.execution.sms
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import com.geofencenotifier.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val jobId = intent.getLongExtra("JOB_ID", -1L)
        if (jobId == -1L) return
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).dao()
                val job = dao.getSmsJobSync(jobId) ?: return@launch
                
                if (job.status != "SENDING") return@launch
                
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        dao.updateSmsJobState(jobId, "SENT", sentAt = System.currentTimeMillis())
                    }
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                        val state = if (job.attemptCount >= 3) "FAILED" else "RETRYING"
                        dao.updateSmsJobState(jobId, state, error = "Generic Failure")
                    }
                    SmsManager.RESULT_ERROR_NO_SERVICE -> {
                        val state = if (job.attemptCount >= 3) "FAILED" else "RETRYING"
                        dao.updateSmsJobState(jobId, state, error = "No Service")
                    }
                    SmsManager.RESULT_ERROR_NULL_PDU -> {
                        val state = if (job.attemptCount >= 3) "FAILED" else "RETRYING"
                        dao.updateSmsJobState(jobId, state, error = "Null PDU")
                    }
                    SmsManager.RESULT_ERROR_RADIO_OFF -> {
                        val state = if (job.attemptCount >= 3) "FAILED" else "RETRYING"
                        dao.updateSmsJobState(jobId, state, error = "Radio Off")
                    }
                    else -> {
                        val state = if (job.attemptCount >= 3) "FAILED" else "RETRYING"
                        dao.updateSmsJobState(jobId, state, error = "Code: $resultCode")
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}