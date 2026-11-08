package com.treesofparis.domain.repository

import com.treesofparis.domain.model.FloweringSheet

/** The flowering reference data: fixed, shipped with the app. */
interface FloweringRepository {
    fun sheets(): List<FloweringSheet>
}
