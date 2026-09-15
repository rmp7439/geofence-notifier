package com.geofencenotifier.execution.sms
import android.telephony.SmsManager
import android.util.Log

class SmsSender {
    fun sendSms(phone: String, message: String) {
        Log.i("SmsSender", "Executing SMS to $phone")
    }
}