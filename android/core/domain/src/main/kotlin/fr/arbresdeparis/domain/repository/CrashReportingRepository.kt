package fr.arbresdeparis.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Whether the app sends crash reports (ADR 0007). Off until the user says yes: the app asks once, at first launch,
 * and the answer can be changed in À propos. The choice is kept.
 */
interface CrashReportingRepository {
    fun observeEnabled(): Flow<Boolean>

    /** False until the user has answered the question, or used the switch in À propos. */
    fun observeAnswered(): Flow<Boolean>

    /** Saves the user's answer: it also counts as having answered the question. */
    suspend fun setEnabled(enabled: Boolean)
}
