package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.Tree
import com.treesofparis.domain.repository.TreeRepository
import javax.inject.Inject

class GetTree
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(id: Long): Tree? = trees.getTree(id)
    }
