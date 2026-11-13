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
- « Arbre le plus proche » only shows once precise location is shared (amended 2026-11-13). From 2026-11-11 it was
  the system location button itself until then, a green pill reading « Près de ma position exacte » (the button's
  text can only come from a system list: « Position exacte », « Utiliser / Partager la position exacte », « Près de
  ma / votre position exacte »). Next to the round « Me localiser », itself a system location button, it read like
  the same button twice. Now « Me localiser » is the only way to share the location; once shared, the app's own
  « Arbre le plus proche » pill appears for the rest of the session. The screen checks the permission again each time
  it comes back to the foreground.
- Manifest (`:feature:map`): `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` with `onlyForLocationButton`, and
  `USE_LOCATION_BUTTON`. The « Réglages » snackbar action after a refusal is only offered on Android 16 and lower.

## Consequences
- No precise-location declaration in the Play Console.
- On Android 17 users tap the location button once per session; the app never sees precise location without that tap.
- The library is **1.0.0-alpha01** (experimental, the only release): pin it and expect API changes. It requires Compose
  `ui` 1.12 / `material3` 1.5 alphas or newer, which the Compose BOM already exceeds.
- The icon-only button has no accessibility label of its own; the app sets « Me localiser » on its container.
- The system draws the button's text in the language of the configuration the app hands it, the phone's language by
  default. The app is French only, so `MainActivity` overrides its locale to French (`useAppLocale`, 2026-11-13,
  [#58](https://github.com/jinqian/treesofparis/issues/58)): « Près de ma position exacte » on an English phone too.
  The permission dialog the system opens after a tap stays in the phone's language.
- Tested on an Android 17 emulator: system dialog on first tap, centring and nearest tree after it, no dialog on the
  second tap. The Android 16 fallback is covered by a Robolectric test.
