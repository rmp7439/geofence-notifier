package com.geofencenotifier.execution.call
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

class CallExecutor(private val context: Context) {
    fun executeCall(phone: String) {
        Log.i("CallExecutor", "Initiating 12-second fixed call to $phone")
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phone")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("CallExecutor", "Failed to start call", e)
        }
    }
}