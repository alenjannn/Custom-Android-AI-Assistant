package com.vincetabelisma.aria.handlers

import android.content.Context
import android.content.Intent
import android.net.Uri

class EmailHandler(private val context: Context) {

    fun composeEmail(to: String, subject: String): String {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return "Opening email to $to"
    }
}
