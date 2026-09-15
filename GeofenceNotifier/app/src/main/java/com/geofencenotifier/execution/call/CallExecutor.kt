package com.geofencenotifier.execution.call
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import kotlinx.coroutines.delay
import com.geofencenotifier.data.db.AppDao

class CallExecutor(private val context: Context, private val dao: AppDao) {
    suspend fun executeFixedDurationCall(jobId: Long, phone: String, durationSeconds: Int): Boolean {
        return try {
            dao.updateCallJobState(jobId, "DIALING", startedAt = System.currentTimeMillis())
            
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phone")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            delay(durationSeconds * 1000L)
            
            var telecomState = "Ended"
            try {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (context.checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    telecomManager.endCall()
                } else {
                    telecomState = "End call restricted by permission"
                }
            } catch (e: Exception) {
                telecomState = "End call failed: ${e.message}"
            }
            
            dao.updateCallJobState(jobId, "ENDED", endedAt = System.currentTimeMillis(), state = telecomState)
            true
        } catch (e: Exception) {
            dao.updateCallJobState(jobId, "FAILED", error = e.message ?: "Unknown Call Error")
            false
        }
    }
}