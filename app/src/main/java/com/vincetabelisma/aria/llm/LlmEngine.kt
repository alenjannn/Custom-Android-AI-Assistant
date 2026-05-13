package com.vincetabelisma.aria.llm

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.util.concurrent.ExecutionException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Singleton entry point for on-device LLM inference via MediaPipe
 * ([com.google.mediapipe.tasks.genai.llminference]).
 *
 * All blocking JNI / model work runs on [Dispatchers.IO]. Only one inference or init runs at a
 * time; concurrent callers are serialized with a [Mutex].
 *
 * NOTE: [LlmInference] is deprecated in tasks-genai ≥ 0.10.33. It still functions correctly;
 * the deprecation warning is suppressed here until migration to the new InferenceModel API.
 */
@Suppress("DEPRECATION")
object LlmEngine {


    private const val TAG = "LlmEngine"
    private const val DEFAULT_MAX_TOKENS = 4096

    @Volatile
    private var inference: LlmInference? = null

    private val gate = Mutex()
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Loads (or reloads) the model from [modelPath] on [Dispatchers.IO]. On failure, logs and
     * leaves the engine uninitialized until a successful call.
     */
    suspend fun init(context: Context, modelPath: String) {
        gate.withLock {
            withContext(Dispatchers.IO) {
                runCatching {
                    inference?.close()
                    inference = null
                    val options =
                        LlmInference.LlmInferenceOptions.builder()
                            .setModelPath(modelPath)
                            .setMaxTokens(DEFAULT_MAX_TOKENS)
                            .build()
                    inference = LlmInference.createFromOptions(context.applicationContext, options)
                }.onFailure { e ->
                    Log.e(TAG, "init failed: ${e.message}", e)
                    inference = null
                }
            }
        }
    }

    /**
     * Runs a full non-streaming generation. Returns an empty string and logs on failure or if
     * [init] did not complete successfully.
     */
    suspend fun generateResponse(prompt: String): String =
        gate.withLock {
            withContext(Dispatchers.IO) {
                val llm = inference
                if (llm == null) {
                    Log.e(TAG, "generateResponse called before successful init")
                    return@withContext ""
                }
                runCatching { llm.generateResponse(prompt) }
                    .onFailure { e -> Log.e(TAG, "generateResponse failed: ${e.message}", e) }
                    .getOrDefault("")
            }
        }

    /**
     * Streams partial decoded text through [onToken]. Calls [onDone] once generation is fully
     * complete. Work is scheduled on [Dispatchers.IO]; [onToken] and [onDone] may be invoked
     * from a MediaPipe background thread — post to main thread before touching UI.
     *
     * Errors are logged only; there is no error callback. Serialization matches
     * [generateResponse].
     */
    fun generateResponseStreaming(
        prompt: String,
        onToken: (String) -> Unit,
        onDone: () -> Unit = {}
    ) {
        engineScope.launch {
            gate.withLock {
                withContext(Dispatchers.IO) {
                    val llm = inference
                    if (llm == null) {
                        Log.e(TAG, "generateResponseStreaming called before successful init")
                        onDone()
                        return@withContext
                    }
                    runCatching {
                        val future =
                            llm.generateResponseAsync(prompt) { partialResult, _ ->
                                onToken(partialResult)
                            }
                        future.get()
                    }
                        .onFailure { e ->
                            when (e) {
                                is ExecutionException ->
                                    Log.e(
                                        TAG,
                                        "generateResponseStreaming failed: ${e.cause?.message}",
                                        e.cause,
                                    )
                                else -> Log.e(TAG, "generateResponseStreaming failed: ${e.message}", e)
                            }
                        }
                    onDone()
                }
            }
        }
    }
}
