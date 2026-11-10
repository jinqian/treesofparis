# 7. Crash reports with Firebase Crashlytics

Status: accepted (2026-11-10).

## Context
- The closed test and the launch need crash reports with readable stack traces. Play Console's Android vitals
  arrive late and only from users who opted in to share usage data with Google.
- The app has no account and no analytics, and the store listing and privacy page say so.
- The repository is public.

## Decision
- **Crashlytics only**: crash and ANR reports, no Firebase Analytics (no usage tracking, so no consent screen).
- **On by default, with an off switch**: À propos → Confidentialité → « Envoyer les rapports de plantage ». The
  choice lives in `CrashReportingRepository` (`:core:domain`); `FirebaseCrashReporting` in `:app` keeps it in
  preferences and passes it to Crashlytics at startup and on every change. Legal basis: legitimate interest.
- **Firebase only in `:app`**: features see the domain interface, never Firebase.
- **`google-services.json` is git-ignored.** The Google Services and Crashlytics Gradle plugins apply only when the
  file exists, so a fresh clone and the CI checks build without it and send nothing. The release workflow writes it
  from the `GOOGLE_SERVICES_JSON` secret and fails without it. The Firebase project registers both
  `fr.arbresdeparis` and `fr.arbresdeparis.debug`.
- Release builds upload the R8 mapping file to Crashlytics.

## Consequences
- Privacy page, store description and the Play Data safety form declare crash logs, diagnostics and device IDs,
  collected, not shared, optional.
- Turning the switch off takes full effect from the next launch (a Crashlytics rule).
- Adding Analytics later needs a consent prompt and a new decision record.
