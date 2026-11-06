package com.treesofparis.feature.about

data class AboutUiState(
    val versionName: String,
)

/** One open-source library row. [url] is the project page, when known. */
data class LibraryUi(
    val name: String,
    val detail: String,
    val url: String?,
)
