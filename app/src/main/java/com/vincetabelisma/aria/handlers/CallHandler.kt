package com.vincetabelisma.aria.handlers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import androidx.core.content.ContextCompat

class CallHandler(private val context: Context) {

    // ── Dial Call ─────────────────────────────────────────────────────────────

    /**
     * Looks up [contactName] in the device's contacts using a case-insensitive
     * contains match, then opens the dialer pre-filled with the found number.
     * Uses ACTION_DIAL (not ACTION_CALL) — no auto-dial, no CALL_PHONE permission needed.
     */
    fun dialCall(contactName: String): String {
        if (contactName.isBlank()) return "Please specify a contact name."

        val matches = queryContacts(contactName)

        return when {
            matches.isEmpty() -> "I couldn't find a contact named \"$contactName\"."
            matches.size > 1  -> {
                val names = matches.joinToString(", ") { it.first }
                "I found multiple contacts named \"$contactName\": $names. Which one did you mean?"
            }
            else -> {
                val (_, number) = matches.first()
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                "Opening dialer for $contactName"
            }
        }
    }

    // ── Answer Call ───────────────────────────────────────────────────────────

    /**
     * Answers the currently ringing call via [TelecomManager].
     * Requires [Manifest.permission.ANSWER_PHONE_CALLS] (declared in AndroidManifest).
     */
    fun answerCall(): String {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return "ARIA needs the 'Answer Phone Calls' permission to do that."
        }

        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

        return try {
            @Suppress("MissingPermission")
            telecom.acceptRingingCall()
            "Answering call"
        } catch (e: IllegalStateException) {
            "No incoming call found"
        } catch (e: Exception) {
            "Couldn't answer the call: ${e.message}"
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Queries [ContactsContract] for all contacts whose display name contains
     * [nameQuery] (case-insensitive). Returns a list of (displayName, phoneNumber) pairs.
     */
    private fun queryContacts(nameQuery: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        val query = nameQuery.lowercase()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val nameCol   = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberCol = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name   = cursor.getString(nameCol) ?: continue
                val number = cursor.getString(numberCol) ?: continue

                if (name.lowercase().contains(query)) {
                    results.add(name to number)
                }
            }
        }

        // De-duplicate by number in case a contact has multiple entries
        return results.distinctBy { it.second }
    }
}
