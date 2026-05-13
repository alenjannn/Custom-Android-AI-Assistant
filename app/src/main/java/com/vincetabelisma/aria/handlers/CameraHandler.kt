package com.vincetabelisma.aria.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore

class CameraHandler(private val context: Context) {

    fun openCamera(): String {
        // Primary: standard camera capture intent
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (context.packageManager.resolveActivity(captureIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            context.startActivity(captureIntent)
            return "Opening camera"
        }

        // Fallback: open whichever app is registered as the system camera
        val cameraAppIntent = Intent("android.media.action.STILL_IMAGE_CAMERA").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (context.packageManager.resolveActivity(cameraAppIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            context.startActivity(cameraAppIntent)
            return "Opening camera"
        }

        return "No camera app found"
    }
}
