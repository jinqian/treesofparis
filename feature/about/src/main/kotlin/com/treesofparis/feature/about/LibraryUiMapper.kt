package com.treesofparis.feature.about

import com.mikepenz.aboutlibraries.entity.Library

internal fun Library.toUi() =
    LibraryUi(
        name = name,
        detail = listOfNotNull(artifactVersion, licenses.firstOrNull()?.name).joinToString(" · "),
        url = website ?: scm?.url,
    )
