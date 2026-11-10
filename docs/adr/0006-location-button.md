# 6. Precise location through the Android location button

Status: accepted (2026-11-10).

## Context
- The app asked for fine and coarse location at launch, then reused the grant for « Me localiser » and « Arbre le
  plus proche ». Both are one-off actions started by a tap.
- Google Play policy for apps targeting Android 17 (API 37), mandatory from 27 January 2027: an app that needs
  precise location only for one-time, user-initiated actions must use the system **location button** and declare
  `ACCESS_FINE_LOCATION` with `usesPermissionFlags="onlyForLocationButton"`. The Play Console declaration for
  precise location is meant for core, persistent uses (navigation), not for « find what is near me ».
  Sources: [Play: Minimum scope and the location button](https://support.google.com/googleplay/android-developer/answer/17033915),
  [Android: location button guide](https://developer.android.com/guide/topics/permissions/private-alternatives/location-button).
- Coarse location (about 1–2 km) is too rough to find the nearest tree.

## Decision
- « Me localiser » is the Jetpack `LocationButton` composable (`androidx.core.locationbutton:locationbutton-compose`),
  icon only, in the round paper look of the map controls. On Android 17 the system draws it and grants precise
  location **for the session**; on Android 16 and lower the same composable shows the usual permission prompt.
- The app no longer asks for location at launch. It centres on the user at launch only when location is already
  granted.
- « Arbre le plus proche » behaves like « Me localiser » (amended 2026-11-11): until precise location is shared, it
  is the system location button itself, a green pill reading « Près de ma position exacte » (the button's text can
  only come from a system list); one tap shares the location and shows the nearest tree. Once shared, it becomes the
  app's own « Arbre le plus proche » pill for the rest of the session. The screen checks the permission again each
  time it comes back to the foreground.
- Manifest (`:feature:map`): `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` with `onlyForLocationButton`, and
  `USE_LOCATION_BUTTON`. The « Réglages » snackbar action after a refusal is only offered on Android 16 and lower.

## Consequences
- No precise-location declaration in the Play Console.
- On Android 17 users tap the location button once per session; the app never sees precise location without that tap.
- The library is **1.0.0-alpha01** (experimental, the only release): pin it and expect API changes. It requires Compose
  `ui` 1.12 / `material3` 1.5 alphas or newer, which the Compose BOM already exceeds.
- The icon-only button has no accessibility label of its own; the app sets « Me localiser » on its container.
- Tested on an Android 17 emulator: system dialog on first tap, centring and nearest tree after it, no dialog on the
  second tap. The Android 16 fallback is covered by a Robolectric test.
