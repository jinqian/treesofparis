package fr.arbresdeparis.feature.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/** Opens Android's share sheet with [text]: the tree's name, place and link (issue #88). */
internal fun Context.shareTree(
    text: String,
    title: String,
) {
    val send =
        Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
            .putExtra(Intent.EXTRA_TITLE, title)
    try {
        startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        // Nothing can receive text: nothing sensible to do.
    }
}
