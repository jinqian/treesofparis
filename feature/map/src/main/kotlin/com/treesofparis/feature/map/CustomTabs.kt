package com.treesofparis.feature.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

/**
 * Opens [url] in a Custom Tab, so the user stays in the app and back returns to the tree sheet. Falls back to any
 * app that can view the link when no browser supports Custom Tabs.
 */
internal fun Context.openInCustomTab(url: String) {
    val uri = url.toUri()
    try {
        CustomTabsIntent
            .Builder()
            .setShowTitle(true)
            .build()
            .launchUrl(this, uri)
    } catch (_: ActivityNotFoundException) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            // No browser at all: nothing sensible to do.
        }
    }
}
