package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.repository.CrashReportingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCrashReporting
    @Inject
    constructor(
        private val crashReporting: CrashReportingRepository,
    ) {
        operator fun invoke(): Flow<Boolean> = crashReporting.observeEnabled()
    }
