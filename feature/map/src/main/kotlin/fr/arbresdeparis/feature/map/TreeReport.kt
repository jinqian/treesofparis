package fr.arbresdeparis.feature.map

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.widget.Toast
import fr.arbresdeparis.core.designsystem.action.composeEmail
import java.util.Locale

/**
 * « Signaler une erreur sur cet arbre »: a message in the user's email app, its subject naming the tree, its body
 * leaving room for what is wrong above a signature with everything needed to find the tree in the City's data.
 * Without an email app, a toast gives the address.
 */
internal fun Context.reportTreeError(
    tree: TreeDetailUi,
    contact: ReportContactUi,
) {
    val opened =
        composeEmail(
            to = contact.email,
            subject = getString(R.string.map_report_subject, tree.id, tree.name),
            body = resources.treeReportBody(tree, contact, androidVersion = Build.VERSION.RELEASE),
        )
    if (!opened) {
        Toast.makeText(this, getString(R.string.map_report_no_app, contact.email), Toast.LENGTH_LONG).show()
    }
}

/**
 * « Bonjour, / Ce qui ne va pas : » then the signature: the tree's id and names, its address, its position, and the
 * app version with the date of its data.
 */
internal fun Resources.treeReportBody(
    tree: TreeDetailUi,
    contact: ReportContactUi,
    androidVersion: String,
): String {
    val treeLine =
        tree.scientificName?.let { getString(R.string.map_report_tree_latin, tree.id, tree.name, it) }
            ?: getString(R.string.map_report_tree, tree.id, tree.name)
    val position = String.format(Locale.ROOT, "%.5f, %.5f", tree.location.lat, tree.location.lon)
    val appLine =
        contact.dataDate?.let { getString(R.string.map_report_app_data, contact.appVersion, it, androidVersion) }
            ?: getString(R.string.map_report_app, contact.appVersion, androidVersion)
    return getString(R.string.map_report_body) + "\n—\n" +
        listOf(treeLine, "${tree.address} · ${tree.area}", position, appLine).joinToString("\n")
}
