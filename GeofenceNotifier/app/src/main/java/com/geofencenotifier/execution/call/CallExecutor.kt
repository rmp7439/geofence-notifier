package com.geofencenotifier.execution.call
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telecom.TelecomManager
import kotlinx.coroutines.delay

class CallExecutor(private val context: Context) {
    suspend fun executeFixedDurationCall(phone: String, durationSeconds: Int = 12) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        delay(durationSeconds * 1000L)
        
        try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            if (context.checkSelfPermission(android.Manifest.permission.ANSWER_PHONE_CALLS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                telecomManager.endCall()
            }
        } catch (e: Exception) {
            // End call failed due to permission or OS limitations
        }
    }
}