package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.Tree
import fr.arbresdeparis.domain.repository.TreeRepository
import javax.inject.Inject

/** Every remarkable tree of the dataset, for the Herbier's Remarquables screen. */
class GetRemarkableTrees
    @Inject
    constructor(
        private val trees: TreeRepository,
    ) {
        suspend operator fun invoke(): List<Tree> = trees.getRemarkableTrees()
    }
