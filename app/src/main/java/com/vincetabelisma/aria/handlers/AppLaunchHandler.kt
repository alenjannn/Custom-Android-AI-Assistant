package com.vincetabelisma.aria.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.vincetabelisma.aria.utils.AriaIntent

class AppLaunchHandler(private val context: Context) {

    fun handle(intent: AriaIntent): String {
        val appName = intent.target.trim()
        if (appName.isBlank()) return "Please specify an app name."

        val pm = context.packageManager

        // Build a list of (appLabel, packageName) for all launchable apps
        val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .mapNotNull { appInfo ->
                val label = pm.getApplicationLabel(appInfo).toString()
                val pkg = appInfo.packageName
                // Only include apps that have a launcher entry
                if (pm.getLaunchIntentForPackage(pkg) != null) label to pkg else null
            }

        val query = appName.lowercase()

        // 1. Exact match first (e.g. "spotify" == "Spotify")
        val exactMatch = installed.firstOrNull { (label, _) ->
            label.lowercase() == query
        }

        // 2. Starts-with match (e.g. "goog" -> "Google Maps")
        val startsMatch = installed.firstOrNull { (label, _) ->
            label.lowercase().startsWith(query)
        }

        // 3. Contains match (e.g. "tube" -> "YouTube")
        val containsMatch = installed.firstOrNull { (label, _) ->
            label.lowercase().contains(query)
        }

        val (matchedLabel, matchedPackage) = exactMatch
            ?: startsMatch
            ?: containsMatch
            ?: return "I couldn't find an app called \"$appName\"."

        val launchIntent = pm.getLaunchIntentForPackage(matchedPackage)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            ?: return "I couldn't find an app called \"$appName\"."

        context.startActivity(launchIntent)
        return "Opening $matchedLabel"
    }
}
