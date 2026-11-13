package fr.arbresdeparis.domain.usecase

import fr.arbresdeparis.domain.repository.CrashReportingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Whether the user has answered « Envoyer les rapports de plantage ? »; until then nothing is sent. */
class ObserveCrashReportingAnswered
    @Inject
    constructor(
        private val crashReporting: CrashReportingRepository,
    ) {
        operator fun invoke(): Flow<Boolean> = crashReporting.observeAnswered()
    }
