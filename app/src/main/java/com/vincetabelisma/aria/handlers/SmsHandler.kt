package com.vincetabelisma.aria.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager

class SmsHandler(private val context: Context) {

    fun sendSms(contactName: String, message: String): String {
        // TODO: Stage 2 — resolve contact number via ContactsContract, then send
        // Placeholder: open the SMS composer pre-filled
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:")
            putExtra("address", contactName)
            putExtra("sms_body", message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return "Opening SMS to $contactName"
    }
}
