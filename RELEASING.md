# Releasing

How to cut a release of Arbres de Paris. There is no release signing or store upload automation yet; this covers
versioning, release notes and tagging, so every build that leaves the repo says what changed.

## Versioning

- `versionName` (in `app/build.gradle.kts`) follows [SemVer](https://semver.org): `0.y.z` until the first public
  release, then `1.0.0`. A new user-facing feature bumps `y` (minor), fixes only bump `z` (patch).
- `versionCode` goes up by exactly one per release, whatever the `versionName` change.
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

1. On an up-to-date `main`, bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Write `fastlane/metadata/android/fr-FR/changelogs/<versionCode>.txt` (≤ 500 characters).
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
6. Build the signed App Bundle with `./gradlew bundleRelease` (see « Release build » below) and upload it with its
   R8 mapping file.

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

In CI, set `TREES_KEYSTORE_FILE`, `TREES_KEYSTORE_PASSWORD`, `TREES_KEY_ALIAS` and `TREES_KEY_PASSWORD` instead.
Without either, the release build is **unsigned** on purpose (it is never silently signed with the debug key, which
Google Play rejects).

**Build and measure.**

```bash
./gradlew bundleRelease    # app/build/outputs/bundle/release/app-release.aab, for Google Play
./gradlew assembleRelease  # app/build/outputs/apk/release/, to measure or test the shrunk app
```

R8 writes `app/build/outputs/mapping/release/mapping.txt`: upload it with each release in the Play Console (App
bundle explorer → Downloads → ReTrace mapping file) so crash reports show real class and line names.

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
