package fr.arbresdeparis.feature.about

data class AboutUiState(
    val versionName: String,
    /** « Nous écrire » writes to this address. */
    val contactEmail: String,
    val websiteUrl: String,
    /** The data extraction the app ships, or null if unknown. */
    val extraction: ExtractionUi? = null,
    /** How many « Guide des Essences de Paris » sheets the flowering months come from. */
    val floweringSheetCount: Int = 0,
    /** « Envoyer les rapports de plantage »: off until the user says yes (ADR 0007). */
    val crashReportingEnabled: Boolean = false,
)

/** « arbresdeparis.fr »: the website address as shown, without its scheme. */
val AboutUiState.websiteLabel: String get() = websiteUrl.substringAfter("://").trimEnd('/')

/** « Extraction du 6 novembre 2026 · 220 339 arbres ». */
data class ExtractionUi(
    val date: String,
    val treeCount: String,
)

/** One open-source library row. [url] is the project page, when known. */
data class LibraryUi(
    val name: String,
    val detail: String,
    val url: String?,
)

/** One guide sheet: « Merisier », « Prunus avium · mars – avril · fleurs voyantes », its PDF. */
data class FloweringSourceUi(
    val commonName: String,
    val detail: String,
    val url: String,
)
