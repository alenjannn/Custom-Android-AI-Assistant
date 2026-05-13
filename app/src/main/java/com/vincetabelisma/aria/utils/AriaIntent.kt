package com.vincetabelisma.aria.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class AriaIntent(
    val action: String = "UNKNOWN",
    val target: String = "",
    val query: String = "",
    val value: String = ""
)

object IntentParser {
    private val gson = Gson()

    fun parse(llmOutput: String): AriaIntent {
        return try {
            // Strip any markdown code fences the model might add
            val cleaned = llmOutput
                .replace("```json", "")
                .replace("```", "")
                .trim()
            gson.fromJson(cleaned, AriaIntent::class.java) ?: AriaIntent()
        } catch (e: JsonSyntaxException) {
            AriaIntent(action = "UNKNOWN")
        }
    }
}