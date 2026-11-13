package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.model.ThemeMode
import fr.arbresdeparis.domain.repository.ThemeRepository
import javax.inject.Inject

class SetThemeMode
    @Inject
    constructor(
        private val theme: ThemeRepository,
    ) {
        suspend operator fun invoke(mode: ThemeMode) = theme.setThemeMode(mode)
    }
