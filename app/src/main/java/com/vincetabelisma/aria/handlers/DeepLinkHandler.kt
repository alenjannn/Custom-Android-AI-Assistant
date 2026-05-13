package com.vincetabelisma.aria.handlers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

class DeepLinkHandler(private val context: Context) {

    // ── YouTube ──────────────────────────────────────────────────────────────

    fun searchYouTube(query: String): String {
        val encoded = Uri.encode(query)

        // Try native YouTube app first
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("youtube://results?search_query=$encoded"))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        // Fall back to browser
        val webIntent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/results?search_query=$encoded"))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }

        return "Searching YouTube for: $query"
    }

    // ── Spotify ───────────────────────────────────────────────────────────────

    fun searchSpotify(query: String): String {
        val encoded = Uri.encode(query)

        // Spotify deep-link uses colons, not query params — do NOT URL-encode the scheme part
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:$encoded"))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        val webIntent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://open.spotify.com/search/$encoded"))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        try {
            context.startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }

        return "Searching Spotify for: $query"
    }

    // ── Web ───────────────────────────────────────────────────────────────────

    fun searchWeb(query: String): String {
        val encoded = Uri.encode(query)

        val intent = Intent(Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=$encoded"))
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        context.startActivity(intent)
        return "Searching the web for: $query"
    }
}
