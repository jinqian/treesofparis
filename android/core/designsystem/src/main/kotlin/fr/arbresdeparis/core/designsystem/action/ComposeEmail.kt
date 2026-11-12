package fr.arbresdeparis.core.designsystem.action

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the user's email app with a message to [to], its [subject] and [body] filled in, ready to edit and send.
 * Only email apps answer a `mailto:` intent. Returns false when the phone has none, so the caller can show the address.
 */
fun Context.composeEmail(
    to: String,
    subject: String,
    body: String,
): Boolean {
    val intent =
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, body)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
