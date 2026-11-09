package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.GeoBounds
import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.repository.TreeRepository
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
            genus: String? = null,
        ): List<Tree> = trees.getTreesInBounds(bounds, limit, remarkableOnly, genus)
    }
