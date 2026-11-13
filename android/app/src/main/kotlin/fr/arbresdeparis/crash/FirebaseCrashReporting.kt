package fr.arbresdeparis.crash

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import fr.arbresdeparis.domain.repository.CrashReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The user's crash-report choice, kept in preferences and passed to Firebase Crashlytics (ADR 0007). Reports are off
 * until the user says yes to the question asked at first launch, or turns them on in À propos; the manifest keeps
 * Crashlytics off until then. A build without `google-services.json` has no Firebase app: the choice is still kept,
 * and nothing is sent.
 */
@Singleton
class FirebaseCrashReporting
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : CrashReportingRepository {
        private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        private val enabled = MutableStateFlow(preferences.getBoolean(KEY_ENABLED, false))
        private val answered = MutableStateFlow(preferences.contains(KEY_ENABLED))

        override fun observeEnabled(): Flow<Boolean> = enabled.asStateFlow()

        override fun observeAnswered(): Flow<Boolean> = answered.asStateFlow()

        override suspend fun setEnabled(enabled: Boolean) {
            preferences.edit { putBoolean(KEY_ENABLED, enabled) }
            this.enabled.value = enabled
            answered.value = true
            applyToCrashlytics()
        }

        /**
         * Gives Crashlytics the saved choice; called when the app starts, before anything can crash and report, and
         * on every change. Crashlytics keeps it across launches. Turned off, the reports still waiting on the phone
         * are deleted rather than sent later.
         */
        fun applyToCrashlytics() {
            if (FirebaseApp.getApps(context).isEmpty()) return
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = enabled.value
            if (!enabled.value) crashlytics.deleteUnsentReports()
        }

        private companion object {
            const val PREFERENCES = "crash_reporting"
            const val KEY_ENABLED = "enabled"
        }
    }
