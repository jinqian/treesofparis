package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.GeoBounds
import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.TreeRepository
import javax.inject.Inject

class GetTreesInBounds
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(
            bounds: GeoBounds,
            limit: Int,
            remarkableOnly: Boolean = false,
        ): List<Tree> = trees.getTreesInBounds(bounds, limit, remarkableOnly)
    }
