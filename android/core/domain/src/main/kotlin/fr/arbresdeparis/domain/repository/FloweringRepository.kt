package fr.arbresdeparis.domain.repository

import fr.arbresdeparis.domain.model.FloweringSheet

/** The flowering reference data: fixed, shipped with the app. */
interface FloweringRepository {
    fun sheets(): List<FloweringSheet>
}
