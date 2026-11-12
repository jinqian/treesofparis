package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.FloweringSeason
import fr.arbresdeparis.domain.model.toSeasons
import fr.arbresdeparis.domain.repository.FloweringRepository
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
