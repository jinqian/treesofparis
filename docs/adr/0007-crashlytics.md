# 7. Crash reports with Firebase Crashlytics

Status: accepted (2026-11-10); amended 2026-11-13 ([#59](https://github.com/jinqian/treesofparis/issues/59)): asked
first instead of on by default.

## Context
- The closed test and the launch need crash reports with readable stack traces. Play Console's Android vitals
  arrive late and only from users who opted in to share usage data with Google.
- The app has no account and no analytics, and the store listing and privacy page say so.
- The repository is public.

## Decision
- **Crashlytics only**: crash and ANR reports, no Firebase Analytics (no usage tracking, so no consent screen).
- **Asked first, off until then** (amended 2026-11-13): at first launch a dialog asks « Envoyer les rapports de
  plantage ? » with two buttons of equal weight, « Envoyer » and « Ne pas envoyer »; it only closes with an answer.
  The manifest sets `firebase_crashlytics_collection_enabled` to false, so nothing is collected before a yes. The
  answer can be changed in À propos → Confidentialité → « Envoyer les rapports de plantage ». It lives in
  `CrashReportingRepository` (`:core:domain`, `observeAnswered`); `FirebaseCrashReporting` in `:app` keeps it in
  preferences and passes it to Crashlytics at startup and on every change; turning it off deletes the reports not
  sent yet. The question is `CrashReportingQuestionRoute` in `:feature:about`, shown by `MainActivity`.
  Legal basis: consent. Crashlytics stores an installation id on the phone, and the CNIL's list of trackers exempt
  from consent (ePrivacy, article 82 of the Loi Informatique et Libertés) does not name crash reporting, so the
  first version's « on by default, legitimate interest » was replaced. Testers who installed 0.1.0 are asked too:
  only a choice made with the switch counts as an answer.
- **No Firebase session events**: `firebase_sessions_enabled` is false in the manifest. Crashlytics 19+ pulls in
  Firebase Sessions, which otherwise sends an event at every launch for crash-free statistics; the app sends crash
  reports only, and the console shows no crash-free users rate.
- **Firebase only in `:app`**: features see the domain interface, never Firebase.
- **`google-services.json` is git-ignored.** The Google Services and Crashlytics Gradle plugins apply only when the
  file exists, so a fresh clone and the CI checks build without it and send nothing. The release workflow writes it
  from the `GOOGLE_SERVICES_JSON` secret and fails without it. The Firebase project registers both
  `fr.arbresdeparis` and `fr.arbresdeparis.debug`.
- Release builds upload the R8 mapping file to Crashlytics.

## Consequences
- Privacy page, store description and the Play Data safety form declare crash logs, diagnostics and device IDs,
  collected, not shared, optional. The privacy page names consent as the legal basis, the transfer to Google in the
  United States (Data Privacy Framework, standard contractual clauses) and how to withdraw consent.
- Fewer reports than on by default: only from users who said yes.
- In the Firebase console: accept the Data Processing and Security Terms and turn off the « share data with Google »
  options.
- Turning the switch off takes full effect from the next launch (a Crashlytics rule).
- Adding Analytics later needs a consent prompt and a new decision record.
