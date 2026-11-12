package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.repository.CrashReportingRepository
import javax.inject.Inject

class SetCrashReporting
    @Inject
    constructor(
        private val crashReporting: CrashReportingRepository,
    ) {
        suspend operator fun invoke(enabled: Boolean) = crashReporting.setEnabled(enabled)
    }
