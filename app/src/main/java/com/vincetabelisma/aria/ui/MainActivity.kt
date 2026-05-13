package com.vincetabelisma.aria.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vincetabelisma.aria.R
import com.vincetabelisma.aria.llm.LlmEngine
import com.vincetabelisma.aria.llm.PromptManager
import com.vincetabelisma.aria.handlers.IntentDispatcher
import com.vincetabelisma.aria.utils.IntentParser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loading = findViewById<ProgressBar>(R.id.loading_indicator)
        val editCommand = findViewById<EditText>(R.id.edit_command)
        val btnParse = findViewById<Button>(R.id.btn_parse)
        val textResult = findViewById<TextView>(R.id.text_result)

        loading.visibility = View.VISIBLE
        btnParse.isEnabled = false

        lifecycleScope.launch {
            // Guard: make sure the model file is actually present on the device
            // before handing off to MediaPipe. A missing file is the most common
            // cause of the "ARIA closed" crash dialog.
            if (!File(MODEL_PATH).exists()) {
                loading.visibility = View.GONE
                textResult.text =
                    "⚠️ Model file not found.\n\nPush the model to the device first:\n" +
                    "  adb push gemma-3n-E2B-it-int4.task $MODEL_PATH"
                return@launch
            }

            runCatching {
                LlmEngine.init(this@MainActivity, MODEL_PATH)
            }.onFailure { e ->
                loading.visibility = View.GONE
                textResult.text = "⚠️ Engine init failed: ${e.message}"
                return@launch
            }

            loading.visibility = View.GONE
            btnParse.isEnabled = true
        }

        btnParse.setOnClickListener {
            val text = editCommand.text?.toString().orEmpty()
            val prompt = PromptManager.buildPrompt(text)

            btnParse.isEnabled = false
            textResult.text = "⏳ Thinking..."

            lifecycleScope.launch {
                val buffer = StringBuilder()
                val done = CompletableDeferred<Unit>()

                // Stream tokens live to the TextView
                LlmEngine.generateResponseStreaming(
                    prompt = prompt,
                    onToken = { token ->
                        buffer.append(token)
                        textResult.post { textResult.text = buffer.toString() }
                    },
                    onDone = { done.complete(Unit) }
                )

                // Suspend until streaming is finished
                done.await()

                // Parse the LLM response into an AriaIntent, then dispatch it
                runCatching {
                    val ariaIntent = IntentParser.parse(buffer.toString())
                    val result = IntentDispatcher(this@MainActivity).dispatch(ariaIntent)
                    ariaIntent to result
                }.onSuccess { (ariaIntent, result) ->
                    textResult.text = "✅ ${ariaIntent.action}\n\n$result"
                }.onFailure { e ->
                    textResult.text = "⚠️ Error: ${e.message}\n\nRaw:\n$buffer"
                }

                btnParse.isEnabled = true
            }
        }
    }

    private companion object {
        const val MODEL_PATH = "/data/local/tmp/gemma-3n-E2B-it-int4.task"
    }
}
