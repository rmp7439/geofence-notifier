package com.geofencenotifier.execution.sms
import android.telephony.SmsManager
import android.util.Log

class SmsSender {
    fun sendSms(phone: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Log.i("SmsSender", "SMS sent successfully to $phone")
        } catch (e: Exception) {
            Log.e("SmsSender", "Failed to send SMS to $phone", e)
        }
    }
}