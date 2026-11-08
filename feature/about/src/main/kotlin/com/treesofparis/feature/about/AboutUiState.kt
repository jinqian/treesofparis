package com.treesofparis.feature.about

data class AboutUiState(
    val versionName: String,
    /** The data extraction the app ships, or null if unknown. */
    val extraction: ExtractionUi? = null,
)

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
