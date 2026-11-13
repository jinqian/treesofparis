package fr.arbresdeparis.domain.repository

import fr.arbresdeparis.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** The theme the user chose in À propos (« Apparence »); the choice is kept. */
interface ThemeRepository {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
