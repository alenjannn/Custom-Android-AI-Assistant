package com.vincetabelisma.aria.handlers

/**
 * Stub — full implementation comes in Stage 3 (Accessibility Service).
 * These functions will be wired to ARIAAccessibilityService to type and send
 * messages inside WhatsApp, Messenger, and Instagram.
 */
object AccessibilityHandler {

    fun sendWhatsApp(target: String, message: String): String {
        // TODO: Stage 3 — use ARIAAccessibilityService to open WhatsApp and send
        return "WhatsApp messaging coming in Stage 3"
    }

    fun sendMessenger(target: String, message: String): String {
        // TODO: Stage 3 — use ARIAAccessibilityService to open Messenger and send
        return "Messenger messaging coming in Stage 3"
    }

    fun sendInstagram(target: String, message: String): String {
        // TODO: Stage 3 — use ARIAAccessibilityService to open Instagram and send
        return "Instagram messaging coming in Stage 3"
    }
}
