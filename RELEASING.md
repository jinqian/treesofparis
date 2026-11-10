# Releasing

How to cut a release of Arbres de Paris: versioning, release notes, tagging and the signed bundle. CI builds and
signs the bundle on each tag and uploads it to Google Play as a draft on the closed testing track.

## Versioning

- The version is `appVersionName` at the top of `app/build.gradle.kts`, in [SemVer](https://semver.org): `0.y.z`
  until the first public release, then `1.0.0`. A new user-facing feature bumps `y` (minor), fixes only bump `z`
  (patch). Minor and patch stay below 100.
- `versionCode` is computed from it: `major × 10,000 + minor × 100 + patch` (0.1.0 → 100, 0.2.0 → 200,
  1.0.0 → 10000). It goes up whenever the version does, as Google Play requires. `./gradlew -q :app:printVersionCode`
  prints it. The release workflow fails if a tag does not match `appVersionName` or its code is not above every
  earlier tag's.
- Each release is tagged `v<versionName>` on `main` (e.g. `v0.1.0`).

## Release notes

Two kinds, written from the same history:

| File | For | Language | How |
|------|-----|----------|-----|
| `CHANGELOG.md` | contributors | English | Generated from Conventional Commits by [git-cliff](https://git-cliff.org) with `cliff.toml`: `feat` → Added, `fix` → Fixed, `refactor` → Changed, `docs` → Documentation; `build`, `ci`, `chore`, `test` and merges are left out. |
| `fastlane/metadata/android/fr-FR/changelogs/<versionCode>.txt` | users (store « Nouveautés ») | French | Written by hand from the changelog: a few lines on what users will notice, **500 characters at most** (Google Play limit). The path is the one fastlane `supply` and F-Droid read. |

Commit messages are the changelog: keep subjects user-meaningful (« show the tree's genus leaf as the detail sheet
watermark », not « update screen »).

## Steps

1. On an up-to-date `main`, bump `appVersionName` in `app/build.gradle.kts`.
2. Write `fastlane/metadata/android/fr-FR/changelogs/<versionCode>.txt` (≤ 500 characters), named after the code
   `./gradlew -q :app:printVersionCode` prints (`100.txt` for 0.1.0).
3. Regenerate the changelog for the new tag:
   ```bash
   git cliff --tag v<versionName> -o CHANGELOG.md   # brew install git-cliff, or pip install git-cliff
   ```
   Read it through; fix a misleading line by rewording the commit before tagging if needed, not by editing the file.
4. Commit as `chore(release): v<versionName>` (excluded from the changelog), open a PR, merge it.
5. Tag the merge commit and push the tag:
   ```bash
   git tag -a v<versionName> -m "Arbres de Paris <versionName>"
   git push origin v<versionName>
   ```
6. The tag starts the Release workflow. It builds the signed App Bundle (see « Release build » below), then uploads
   it to Google Play with its R8 mapping file and the French note, as a **draft** on the closed testing track
   (`alpha`). See « Google Play upload » below.
7. In the Play Console (Test and release → Closed testing), check the draft and send it for review / roll it out.

To upload to another track, or to roll out at once, run the workflow from the Actions tab (Release → Run workflow)
and pick the track and status. Untick « Upload to Google Play » to only build the bundle. Locally,
`./gradlew bundleRelease` builds the same bundle with `keystore.properties`.

Between releases, `git cliff -o CHANGELOG.md` refreshes the « Unreleased » section.

## Release build

The `release` build type runs R8 (code shrinking, optimisation, obfuscation) and resource shrinking. Rules live in
`app/proguard-rules.pro`; the libraries ship their own (Room, Hilt, Compose, kotlinx.serialization, Navigation 3).
CI builds an unsigned release APK on every run, so a missing R8 rule fails the PR, not the store upload.

**Signing.** Google Play wants the bundle signed with your *upload key* (Play App Signing then re-signs it for users).
Create the key once, keep it and its passwords out of the repository (`*.jks` and `keystore.properties` are
git-ignored) and back them up:

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```

Then create `keystore.properties` at the repository root:

```properties
storeFile=upload-keystore.jks
storePassword=…
keyAlias=upload
keyPassword=…
```

Gradle also reads `TREES_KEYSTORE_FILE`, `TREES_KEYSTORE_PASSWORD`, `TREES_KEY_ALIAS` and `TREES_KEY_PASSWORD` from
the environment. Without either, the release build is **unsigned** on purpose (it is never silently signed with the
debug key, which Google Play rejects).

**Signing in CI.** `.github/workflows/release.yml` builds the signed bundle on every `v*` tag (or from the Actions tab
with « Run workflow ») and attaches `app-release.aab` and `mapping.txt` to the run for 90 days. It needs four
repository secrets for signing (Settings → Secrets and variables → Actions), set once from the repository root:

```bash
base64 -i upload-keystore.jks | gh secret set TREES_KEYSTORE_BASE64
gh secret set TREES_KEYSTORE_PASSWORD   # each prompts for its value
gh secret set TREES_KEY_ALIAS
gh secret set TREES_KEY_PASSWORD
```

The workflow writes the key to the runner's temp directory, never to the workspace, and deletes it at the end.

**Firebase config.** Crashlytics needs `app/google-services.json` (ADR 0007). It is git-ignored: download it from the
Firebase console (project settings → your apps), with both `fr.arbresdeparis` and `fr.arbresdeparis.debug`
registered, and copy it to `app/`. Without it the app builds and sends nothing. The release workflow writes it from a
fifth secret and fails without it:

```bash
base64 -i app/google-services.json | gh secret set GOOGLE_SERVICES_JSON
```

Replace the secret when the file changes (a new app registered, for example).

**Google Play upload.** The workflow's last step runs fastlane `supply`, which uploads the bundle, `mapping.txt`
and `fastlane/metadata/android/fr-FR/changelogs/<versionCode>.txt` (the step fails if that note is missing). It
never changes the store listing, the description or the images. It signs in with a Google Cloud service account:

1. In [Google Cloud](https://console.cloud.google.com), pick (or create) a project, enable the **Google Play Android
   Developer API**, then IAM → Service accounts → create one (no role needed) → Keys → Add key → JSON.
2. In the Play Console → Users and permissions → Invite new user: the service account's email, with access to
   Arbres de Paris only and the « Release to testing tracks » permission (add « Release to production » later, if
   CI should also publish to production).
3. Store the key as a secret, then delete the downloaded file:
   ```bash
   base64 -i play-service-account.json | gh secret set PLAY_SERVICE_ACCOUNT_JSON
   ```

Two Google Play limits:
- **The first bundle goes up by hand.** The API can only add releases to an app that already has one: upload the
  first bundle in the Play Console (download it from the workflow run, with « Upload to Google Play » unticked).
- **Drafts only until the app is reviewed.** While the app has never been published, Google Play accepts only
  `draft` releases from the API, so keep the `draft` status; `completed` works once the app is live.

**Build and measure.**

```bash
./gradlew bundleRelease    # app/build/outputs/bundle/release/app-release.aab, for Google Play
./gradlew assembleRelease  # app/build/outputs/apk/release/, to measure or test the shrunk app
```

R8 writes `app/build/outputs/mapping/release/mapping.txt`. With the Firebase config present, the build uploads it to
Crashlytics itself (`uploadCrashlyticsMappingFileRelease`). Play reads it from the bundle; keep the copy CI attaches.

**Testing a release build on a device** without the upload key: sign a copy with the debug key, outside Gradle.

```bash
BT=$ANDROID_HOME/build-tools/<version>
$BT/zipalign -f -p 4 app/build/outputs/apk/release/app-release-unsigned.apk /tmp/aligned.apk
$BT/apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android --out /tmp/release-test.apk /tmp/aligned.apk
adb install -r /tmp/release-test.apk
```

Check at least: the map and pins, a tree sheet and its Wikipedia link, Herbier, adding a favourite, À propos →
libraries, and that the app comes back on the same tab after `adb shell am kill fr.arbresdeparis` in the background.

**Size (0.1.0, 2026-11-08).** Release APK 12.6 MB (21.0 MB before R8): code 1.2 MB, `trees.db` 10.0 MB, fonts
1.0 MB, resources 0.2 MB (compressed sizes). App Bundle 14.8 MB; Play serves per-device APKs close to the APK size.
