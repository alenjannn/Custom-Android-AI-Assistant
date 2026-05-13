# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── MediaPipe GenAI / LLM Inference ──────────────────────────────────────────
# Prevents R8 from stripping JNI-bound classes that MediaPipe resolves at
# runtime through native code. Removing these causes "ARIA closed" crashes.
-keep class com.google.mediapipe.** { *; }
-keep interface com.google.mediapipe.** { *; }
-keepclassmembers class com.google.mediapipe.** { *; }

# Flatbuffers used internally by MediaPipe task files
-keep class com.google.flatbuffers.** { *; }

# Guava futures used by LlmInference async API
-keep class com.google.common.util.concurrent.** { *; }

# Gson (used by IntentParser)
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.vincetabelisma.aria.utils.AriaIntent { *; }