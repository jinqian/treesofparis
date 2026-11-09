package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.repository.TreeRepository
import javax.inject.Inject

class GetTree
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(id: Long): Tree? = trees.getTree(id)
    }
