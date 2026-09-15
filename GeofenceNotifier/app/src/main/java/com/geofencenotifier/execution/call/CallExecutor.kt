package com.geofencenotifier.execution.call
import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.delay

class CallExecutor(private val context: Context) {
    suspend fun executeFixedDurationCall(phone: String, durationSeconds: Int = 12) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        delay(durationSeconds * 1000L)
        // Telecom termination logic goes here through bound Abstraction per SDK version.
    }
}