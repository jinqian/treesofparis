package fr.arbresdeparis.domain.model

/** Facts about the installed app, provided by :app (e.g. from BuildConfig). */
data class AppInfo(
    val versionName: String,
    /** Where users write to: « Nous écrire » and error reports. */
    val contactEmail: String,
    /** The app's website, https://arbresdeparis.fr. */
    val websiteUrl: String,
)
