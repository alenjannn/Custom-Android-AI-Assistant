// Skeleton — fill handler calls as you build each one
package com.vincetabelisma.aria.handlers

import android.content.Context
import com.vincetabelisma.aria.utils.AriaIntent

class IntentDispatcher(private val context: Context) {

    fun dispatch(intent: AriaIntent): String {
        return when (intent.action) {
            "OPEN_APP"        -> AppLaunchHandler(context).handle(intent)
            "SEARCH_YOUTUBE"  -> DeepLinkHandler(context).searchYouTube(intent.query)
            "SEARCH_SPOTIFY"  -> DeepLinkHandler(context).searchSpotify(intent.query)
            "SEARCH_WEB"      -> DeepLinkHandler(context).searchWeb(intent.query)
            "SET_ALARM"       -> AlarmHandler(context).setAlarm(intent.value)
            "SET_TIMER"       -> AlarmHandler(context).setTimer(intent.value)
            "OPEN_CAMERA"     -> CameraHandler(context).openCamera()
            "DIAL_CALL"       -> CallHandler(context).dialCall(intent.target)
            "SEND_SMS"        -> SmsHandler(context).sendSms(intent.target, intent.query)
            "SEND_WHATSAPP"   -> AccessibilityHandler.sendWhatsApp(intent.target, intent.query)
            "SEND_MESSENGER"  -> AccessibilityHandler.sendMessenger(intent.target, intent.query)
            "SEND_INSTAGRAM"  -> AccessibilityHandler.sendInstagram(intent.target, intent.query)
            "COMPOSE_EMAIL"   -> EmailHandler(context).composeEmail(intent.target, intent.query)
            "CHAT"            -> "CHAT_MODE" // Handled separately by LLM
            "UNKNOWN"         -> "I didn't understand that. Could you rephrase?"
            else              -> "Unknown action: ${intent.action}"
        }
    }
}