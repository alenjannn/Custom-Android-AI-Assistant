package com.vincetabelisma.aria.handlers

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

class AlarmHandler(private val context: Context) {

    // ── Set Alarm ─────────────────────────────────────────────────────────────

    /**
     * [timeString] must be in HH:MM format (e.g. "08:00", "14:30").
     * Launches the system alarm clock with the parsed hour and minute.
     */
    fun setAlarm(timeString: String): String {
        val parts = timeString.trim().split(":")
        if (parts.size != 2) return "Please provide the time in HH:MM format (e.g. \"07:30\")."

        val hour   = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()

        if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
            return "Invalid time \"$timeString\". Use HH:MM format (e.g. \"07:30\")."
        }

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)   // set silently without opening Clock app
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        return "Alarm set for $timeString"
    }

    // ── Set Timer ─────────────────────────────────────────────────────────────

    /**
     * [secondsString] is the total number of seconds (e.g. "300" = 5 minutes).
     * Starts the timer immediately via EXTRA_SKIP_UI.
     */
    fun setTimer(secondsString: String): String {
        val totalSeconds = secondsString.trim().toIntOrNull()
            ?: return "I couldn't understand the timer duration \"$secondsString\"."

        if (totalSeconds <= 0) return "Timer duration must be greater than zero."

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, totalSeconds)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)   // starts immediately
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        return "Timer set for ${formatDuration(totalSeconds)}"
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Converts a raw second count into a human-readable string. */
    private fun formatDuration(seconds: Int): String {
        val hours   = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs    = seconds % 60

        return buildString {
            if (hours > 0)   append("$hours ${if (hours == 1) "hour" else "hours"}")
            if (minutes > 0) {
                if (isNotEmpty()) append(" ")
                append("$minutes ${if (minutes == 1) "minute" else "minutes"}")
            }
            if (secs > 0 || isEmpty()) {
                if (isNotEmpty()) append(" ")
                append("$secs ${if (secs == 1) "second" else "seconds"}")
            }
        }
    }
}
