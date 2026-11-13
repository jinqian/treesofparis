---
name: release
description: Prepare a release of Arbres de Paris (Android app) — pick the next version from the commits since the last
  tag, bump appVersionName, write the French Google Play note (≤ 500 characters), regenerate CHANGELOG.md, open the
  `chore(release)` PR, then tag the merge commit so CI uploads the signed bundle to Google Play as a draft. Use when the
  user asks to prepare, cut, ship or tag a release or a new version (« prépare une release », « v0.3.0 »).
---

# Preparing a release

The reference is `android/RELEASING.md`: read it first, it wins if this skill and it ever disagree. This skill is the
checklist around it, with the checks that keep a release from going wrong. CI does the build, the signing and the
upload (`.github/workflows/release.yml`); a release here is a version bump PR, then a tag.

**Ask the user before each step that can't be undone or that others see:** the version number, the French note,
merging the PR, and pushing the tag. Version tags `v*` are **immutable** (repository ruleset): a tag pushed on the
wrong commit or with the wrong version can never be moved or deleted, only superseded by a new version.

## 1. Where things stand

```bash
git fetch origin --tags
git switch main && git pull --ff-only
git status --short                           # must be clean
git describe --tags --abbrev=0               # the last release, e.g. v0.2.0
gh run list --branch main --workflow CI --limit 3   # main must be green
gh pr list --state open                      # anything the user wants in this release?
```

Stop and tell the user if `main` is red, the tree is dirty, or an open PR looks like it belongs in the release.

## 2. What changed, and the version

```bash
LAST=$(git describe --tags --abbrev=0)
git log "$LAST"..origin/main --no-merges --format='%s'
git diff --stat "$LAST"..origin/main -- android/ | tail -1
git diff "$LAST"..origin/main -- android/core/data/trees-db-version.txt
```

- Sort the subjects by scope. Only Android scopes reach users of the app (`map`, `favorites`, `about`, `herbier`,
  `app`, `data`, `designsystem`, `domain`); `website`, `ci`, `build`, `docs`, `test`, `chore` don't.
- No Android change since the last tag: there is nothing to release. Say so.
- SemVer (`0.y.z` until the first public release): a user-facing `feat` → minor (`0.2.0` → `0.3.0`); only `fix` →
  patch (`0.2.0` → `0.2.1`). Minor and patch stay below 100.
- A new `trees-db-version.txt` means testers' tree data is replaced on update; favourites live in `user.db` and stay.
  A new `UserDatabase` version needs a migration: check one exists and is tested before going further.

Propose the version with a one-line reason and the user-facing changes, and wait for the user's answer.

## 3. The release branch

```bash
git switch -c chore/release-v<version>
```

Bump `val appVersionName = "…"` at the top of `android/app/build.gradle.kts`, then:

```bash
cd android && ./gradlew -q :app:printVersionCode   # e.g. 300 for 0.3.0 — the note's file name
```

## 4. The French note (Google Play « Nouveautés »)

File: `android/fastlane/metadata/android/fr-FR/changelogs/<versionCode>.txt`. The Release workflow fails without it.

- Read the earlier notes in that folder first and match them: a bullet `• ` per change, what users will **see or
  do**, in plain French (« Touchez l'adresse d'un arbre pour l'ouvrir dans votre application de cartes. »), never
  commit wording, class names or internals. Fixes that users noticed get one line; invisible ones are left out.
- UI terms exactly as the app says them (« Son histoire », « Arbre le plus proche », « Remarquables »); check
  `android/feature/*/src/main/res/values/strings.xml` when unsure. French typography: « » with spaces, ’ or ' as the
  earlier notes do, no English.
- End with a short thank-you or invitation when it fits (« Merci pour vos retours ! »).
- **500 characters at most** (Google Play). Count characters, not bytes (`wc -c` overcounts accents):
  ```bash
  python3 -c "import sys; t=open(sys.argv[1], encoding='utf-8').read().rstrip('\n'); print(len(t))" <file>
  ```

Show the note and its length to the user and wait for their approval or edits.

## 5. The changelog

```bash
git cliff --tag v<version> -o CHANGELOG.md        # from the repository root (brew install git-cliff)
git diff CHANGELOG.md
```

Read the new section. A misleading line comes from its commit message: tell the user rather than editing the file
by hand (RELEASING.md). `chore(release)` commits are left out by `cliff.toml`.

## 6. Check, commit, PR

```bash
cd android && ./gradlew assembleRelease   # catches a missing R8 rule before the tag does
cd .. && git add android/app/build.gradle.kts android/fastlane CHANGELOG.md
git commit -m "chore(release): v<version>"
git push -u origin chore/release-v<version>
gh pr create --title "chore(release): v<version>" --body "…"
```

The PR body: the version and code, the user-facing changes, the French note, and whether tree data or the user
database changes. End it with the attribution line the session asks for.

Wait for CI (`gh pr checks <n> --watch`), then **ask the user** before merging (`gh pr merge <n> --merge
--delete-branch`, merge commits as the other PRs).

## 7. The tag (immutable — check twice)

After the merge, on the merge commit:

```bash
git switch main && git pull --ff-only
sed -n 's/^val appVersionName = "\(.*\)"$/\1/p' android/app/build.gradle.kts   # must print <version>
git log -1 --format='%h %s'                                                    # the release PR's merge commit
ls android/fastlane/metadata/android/fr-FR/changelogs/                         # <versionCode>.txt is there
```

Show these to the user and **ask before pushing the tag**:

```bash
git tag -a v<version> -m "Arbres de Paris <version>"
git push origin v<version>
```

## 8. The upload

The tag starts the Release workflow: version check, signed bundle, then fastlane `supply` uploads the bundle, its R8
mapping and the note as a **draft** on closed testing (`alpha`).

```bash
gh run list --workflow Release --limit 1
gh run watch <run-id> --exit-status
```

When it is green, tell the user: the draft is in the Play Console (Test and release → Closed testing); **they** check
it and send it for review — leave review, rollout and store listing changes to them. If the workflow fails, read the
log (`gh run view <run-id> --log-failed`) and fix forward: a new commit and, if the tag itself was wrong, a new patch
version — never try to move a tag.

## Don'ts

- Don't tag a commit that is not on `main`, or before the release PR is merged.
- Don't edit `CHANGELOG.md` by hand or commit the note with more than 500 characters.
- Don't touch signing keys, `keystore.properties`, `google-services.json` or the service account file; they are
  git-ignored and live in repository secrets.
- Don't change the track or status (`alpha`, `draft`) unless the user asks; until the app is published, Google Play
  only accepts drafts from the API.
