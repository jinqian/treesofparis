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
6. Build the release artifact with `./gradlew bundleRelease` (signing to be set up before the first store release).

Between releases, `git cliff -o CHANGELOG.md` refreshes the « Unreleased » section.
