# Changelog

## 1.0.1 — launch crash fixed

**Root cause.** `windowBackground` in `Theme.SpaceWalls` pointed at
`@drawable/splash_background`, a `<layer-list>` whose second item was a
`<bitmap android:src="@drawable/ic_splash_mark">`. `ic_splash_mark` is a **vector**
drawable, and `<bitmap>` only accepts bitmap sources. Inflating the window background
therefore threw before the first frame, so the app crashed on every launch — no UI ever
appeared.

**Fix.**
- `splash_background.xml` now places the vector directly in a `<layer-list>` item
  (`<item android:drawable="@drawable/ic_splash_mark" android:gravity="center" />`),
  with a comment explaining why `<bitmap>` must not be used here.
- Legacy launcher icons added for `mdpi … xxxhdpi`. Previously only
  `mipmap-anydpi-v26` existed, so `@mipmap/ic_launcher` did not resolve on Android 7–7.1.
- `WorkManager` scheduling in `Application.onCreate` is wrapped in `runCatching` so a
  scheduling failure can never take down startup.
- Startup smoke tests added (`app/src/test/.../AppStartupTest.kt`), run with Robolectric:
  one asserts the theme's window background inflates, the other launches `MainActivity`
  for real. Both pass.

**Verified.** `./gradlew :app:testDebugUnitTest` → 2 tests, 0 failures.
`./gradlew :app:assembleDebug` → APK v1.0.1 (versionCode 2), `<bitmap>` no longer present
in the compiled splash drawable.

## 1.0.0 — first release
- Live catalogue from Wikimedia Commons + NASA, licence verification, downloads,
  wallpaper setting, favourites, offline cache, daily wallpaper, AMOLED Compose UI.
