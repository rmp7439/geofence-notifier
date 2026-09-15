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
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phone")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            delay(durationSeconds * 1000L)
            
            var telecomState = "API_ENDED"
            var finalStatus = "ENDED"
            try {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (context.checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val result = telecomManager.endCall()
                    if (!result) {
                        telecomState = "END_FAILED (System Restricted)"
                        finalStatus = "FAILED"
                    }
                } else {
                    telecomState = "END_RESTRICTED (Missing Permission)"
                    finalStatus = "FAILED"
                }
            } catch (e: Exception) {
                telecomState = "END_FAILED (${e.message})"
                finalStatus = "FAILED"
            }
            
            dao.updateCallJobState(jobId, finalStatus, endedAt = System.currentTimeMillis(), state = telecomState)
            true // Don't retry if initiation succeeded but termination failed
        } catch (e: SecurityException) {
            dao.updateCallJobState(jobId, "FAILED", error = "Missing CALL_PHONE permission")
            false
        } catch (e: Exception) {
            dao.updateCallJobState(jobId, "RETRYING", error = e.message ?: "Unknown Call Error")
            false
        }
    }
}