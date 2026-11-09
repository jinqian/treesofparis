package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.FloweringSheet
import fr.arbresdeparis.domain.repository.FloweringRepository
import javax.inject.Inject

/** The guide sheets the flowering months are read from, by scientific name. */
class GetFloweringSources
    @Inject
    constructor(
        private val flowering: FloweringRepository,
    ) {
        operator fun invoke(): List<FloweringSheet> = flowering.sheets().sortedBy { it.scientificName }
    }
