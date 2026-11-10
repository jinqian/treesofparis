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
 * The user's crash-report choice, kept in preferences and passed to Firebase Crashlytics (ADR 0007). Reports are on
 * until the user turns them off in À propos. A build without `google-services.json` has no Firebase app: the choice
 * is still kept, and nothing is sent.
 */
@Singleton
class FirebaseCrashReporting
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : CrashReportingRepository {
        private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        private val enabled = MutableStateFlow(preferences.getBoolean(KEY_ENABLED, true))

        override fun observeEnabled(): Flow<Boolean> = enabled.asStateFlow()

        override suspend fun setEnabled(enabled: Boolean) {
            preferences.edit { putBoolean(KEY_ENABLED, enabled) }
            this.enabled.value = enabled
            applyToCrashlytics()
        }

        /** Gives Crashlytics the saved choice; called when the app starts, before anything can crash and report. */
        fun applyToCrashlytics() {
            if (FirebaseApp.getApps(context).isEmpty()) return
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled.value
        }

        private companion object {
            const val PREFERENCES = "crash_reporting"
            const val KEY_ENABLED = "enabled"
        }
    }
