package fr.arbresdeparis.domain.repository

import kotlinx.coroutines.flow.Flow

/** Whether the app sends crash reports. On until the user turns it off in À propos; the choice is kept. */
interface CrashReportingRepository {
    fun observeEnabled(): Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)
}
