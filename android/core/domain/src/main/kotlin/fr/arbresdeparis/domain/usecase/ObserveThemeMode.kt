package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeMode
    @Inject
    constructor(
        private val theme: ThemeRepository,
    ) {
        operator fun invoke(): Flow<ThemeMode> = theme.observeThemeMode()
    }
