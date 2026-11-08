package com.treesofparis.domain.model

import java.time.Instant

/**
 * Which extraction of the « Les arbres » dataset the app ships, provided by :core:data from the metadata written by
 * the data script. [extractedAt] is null if the metadata is missing.
 */
data class DatasetInfo(
    val extractedAt: Instant?,
    val treeCount: Int,
)
