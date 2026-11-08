package com.treesofparis.domain.usecase

import com.treesofparis.domain.model.FloweringSeason
import com.treesofparis.domain.model.toSeasons
import com.treesofparis.domain.repository.FloweringRepository
import javax.inject.Inject

/** Every genus with a flowering calendar, in the order of the year. */
class GetFloweringSeasons
    @Inject
    constructor(
        private val flowering: FloweringRepository,
    ) {
        operator fun invoke(): List<FloweringSeason> = flowering.sheets().toSeasons()
    }

/** When [genus] flowers, or null when the guide gives no calendar for it. */
class GetFloweringSeason
    @Inject
    constructor(
        private val flowering: FloweringRepository,
    ) {
        operator fun invoke(genus: String?): FloweringSeason? =
            genus?.let { name ->
                flowering
                    .sheets()
                    .filter { it.genus == name }
                    .toSeasons()
                    .firstOrNull()
            }
    }
