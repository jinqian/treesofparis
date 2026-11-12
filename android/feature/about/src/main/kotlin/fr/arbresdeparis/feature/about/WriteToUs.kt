package fr.arbresdeparis.feature.about

import android.content.Context
import android.os.Build
import android.widget.Toast
import fr.arbresdeparis.core.designsystem.action.composeEmail

/**
 * « Nous écrire »: a message to [email] in the user's email app, signed with the app and Android versions so a reply
 * can take them into account. Without an email app, a toast gives the address.
 */
internal fun Context.writeToUs(
    email: String,
    versionName: String,
) {
    val opened =
        composeEmail(
            to = email,
            subject = getString(R.string.about_contact_subject),
            body = getString(R.string.about_contact_body, versionName, Build.VERSION.RELEASE),
        )
    if (!opened) Toast.makeText(this, getString(R.string.about_contact_no_app, email), Toast.LENGTH_LONG).show()
}
