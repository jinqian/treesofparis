package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.TreeRepository
import javax.inject.Inject

/** Every remarkable tree of the dataset, for the Herbier's Remarquables screen. */
class GetRemarkableTrees
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(): List<Tree> = trees.getRemarkableTrees()
    }
