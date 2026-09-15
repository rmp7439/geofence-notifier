package com.geofencenotifier.execution.call
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import kotlinx.coroutines.delay
import com.geofencenotifier.data.db.AppDao

enum class CallResult {
    SUCCESS,
    INITIATION_FAILED,
    TERMINATION_FAILED,
    TERMINATION_RESTRICTED,
    RETRYABLE_FAILURE
}

class CallExecutor(private val context: Context, private val dao: AppDao) {
    suspend fun executeFixedDurationCall(jobId: Long, phone: String, durationSeconds: Int): CallResult {
        return try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phone")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            delay(durationSeconds * 1000L)
            
            var telecomState = "API_ENDED"
            var finalStatus = "ENDED"
            var callResult = CallResult.SUCCESS
            
            try {
                val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                if (context.checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    val result = telecomManager.endCall()
                    if (!result) {
                        telecomState = "END_FAILED (System Restricted)"
                        finalStatus = "TERMINATION_FAILED"
                        callResult = CallResult.TERMINATION_FAILED
                    }
                } else {
                    telecomState = "END_RESTRICTED (Missing Permission)"
                    finalStatus = "TERMINATION_RESTRICTED"
                    callResult = CallResult.TERMINATION_RESTRICTED
                }
            } catch (e: Exception) {
                telecomState = "END_FAILED (${e.message})"
                finalStatus = "TERMINATION_FAILED"
                callResult = CallResult.TERMINATION_FAILED
            }
            
            dao.updateCallJobState(jobId, finalStatus, endedAt = System.currentTimeMillis(), state = telecomState)
            callResult
        } catch (e: SecurityException) {
            dao.updateCallJobState(jobId, "INITIATION_FAILED", error = "Missing CALL_PHONE permission")
            CallResult.INITIATION_FAILED
        } catch (e: Exception) {
            dao.updateCallJobState(jobId, "RETRYING", error = e.message ?: "Unknown Call Error")
            CallResult.RETRYABLE_FAILURE
        }
    }
}