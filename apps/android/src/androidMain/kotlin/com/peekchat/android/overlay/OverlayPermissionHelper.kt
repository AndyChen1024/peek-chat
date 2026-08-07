package com.peekchat.android.overlay

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Overlay permission utility.
 *
 * Android SYSTEM_ALERT_WINDOW is a special permission that MUST be granted
 * manually by the user in system settings. We guide them there with a
 * branded explanation card before the jump (per Iris's UX spec).
 *
 * Atlas's 2-strike note: after 2 quick denies, Android rate-limits further
 * requests. We detect denial and show a persistent in-app prompt instead
 * of re-popping the system dialog.
 */
object OverlayPermissionHelper {

    /** Check if overlay permission is currently granted. */
    fun isGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // pre-M: granted at install time
        }
    }

    /**
     * Create an intent that takes the user to the system overlay settings page
     * for our app. Call with startActivity or startActivityForResult.
     */
    fun createSettingsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
