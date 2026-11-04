# Verification record

What was actually executed while building this project, and what still needs a device.

## 1. Toolchain / build

| Check | Result |
|---|---|
| JDK | Temurin 17.0.21 |
| Gradle | 8.7 (wrapper generated) |
| Android Gradle Plugin | 8.6.1 |
| Kotlin | 2.0.21, KSP 2.0.21-1.0.25 |
| SDK installed | Platform 35, Build-Tools 35.0.0 / 34.0.0 |
| `:app:assembleDebug` | **BUILD SUCCESSFUL** |
| Artifact | `app-debug.apk`, 12.2 MB, 14 dex files |
| Signature | verified with `apksigner verify` — Android Debug key (installable) |
| Manifest | `com.spacewalls.app.debug`, minSdk 24, targetSdk 35, label "SpaceWalls" |
| Permissions | INTERNET, ACCESS_NETWORK_STATE, SET_WALLPAPER, SET_WALLPAPER_HINTS (+ WorkManager's) |

## 2. Live API integration

Both providers were queried for real during development and their response shapes were used
verbatim in the DTOs (no invented fields):

- `commons.wikimedia.org/w/api.php` — `generator=search`, `prop=imageinfo`,
  `iiprop=url|size|mime|extmetadata`, `formatversion=2`. Continuation confirmed:
  `continue.gsroffset` returns a disjoint next page (`page 1: 1164360…176978131`,
  `page 2: 36064…155739988`).
- `images-api.nasa.gov/search` — `media_type=image`, `page`, `page_size`, `metadata.total_hits`.
  Item `href` returns the asset manifest (`…~orig.jpg`, `~medium.jpg`, `~small.jpg`, `~thumb.jpg`),
  which the app uses to resolve originals; `links[rel=canonical]` is preferred when present.

## 3. Rights-verification audit (live data, 240 images)

The rules in `LicensePolicy.kt` were mirrored and run against 6 collections × 20 results per provider:

| Provider | Passed verification |
|---|---|
| Wikimedia Commons | **94 / 120** |
| NASA Image and Video Library | **75 / 120** |

Rejections: `nasa:resolution` 45, `resolution` 16, `mime` 7, `unrecognised licence` 3.
That is the intended behaviour — sub-1600 px files, non-bitmap formats and licences the app cannot
read are dropped rather than guessed at.

Example of a fully-attributed item that passes:

```
File: Orion Nebula - Hubble 2006 mosaic 18000.jpg
creator  : NASA, ESA, M. Robberto (STScI/ESA) and the HST Orion Treasury Project Team
licence  : Public domain
page     : https://commons.wikimedia.org/wiki/File:Orion_Nebula_-_Hubble_2006_mosaic_18000.jpg
size     : 18000 × 18000
```

## 4. Needs a real device / emulator (not verifiable in this environment)

- Rendering and scroll performance of the Compose UI.
- `WallpaperManager` actually applying home / lock / both (requires a device with a wallpaper service).
- WorkManager download progress + MediaStore publish to `Pictures/SpaceWalls`.
- Coil disk cache behaviour across process death.

Everything above is implemented against documented platform APIs and compiles into a signed APK;
it could not be executed here because this sandbox has no Android runtime or emulator.

## 5. Launch crash — root cause and fix (1.0.1)

v1.0.0 crashed on every launch before the first frame was drawn.

| | |
|---|---|
| Symptom | App closes immediately; no UI |
| Cause | `Theme.SpaceWalls` → `windowBackground` = `@drawable/splash_background`; that layer-list contained `<bitmap android:src="@drawable/ic_splash_mark">`, and `ic_splash_mark` is a **vector**. `<bitmap>` accepts only bitmap drawables, so window inflation threw. |
| Evidence | `aapt2 dump xmltree --file res/drawable/splash_background.xml` on the shipped APK showed `E: bitmap … src=@0x7f05000a`, and `aapt2 dump resources` resolved `0x7f05000a` to `drawable/ic_splash_mark … type=XML`. |
| Fix | Vector placed directly in a layer-list item; no `<bitmap>` wrapper. |

Secondary hardening in the same release: legacy `mipmap-*dpi` launcher icons for
Android 7–7.1 (only `mipmap-anydpi-v26` existed before), and `runCatching` around
WorkManager scheduling at startup.

### Regression tests

`app/src/test/java/com/spacewalls/app/AppStartupTest.kt` (Robolectric 4.15.1, SDK 34):

```
AppStartupTest > themeWindowBackgroundInflates        PASSED
AppStartupTest > mainActivityLaunchesWithoutCrashing  PASSED
2 tests completed, 0 failures
```

The second test builds the real `MainActivity`, so it exercises `Application.onCreate`,
the theme, Compose composition of the home screen, the ViewModels and the Room database.

Re-checked on the 1.0.1 APK: `aapt2 dump xmltree` shows **no** `<bitmap>` element in
`splash_background.xml`, and `mipmap-mdpi … mipmap-xxxhdpi` all contain
`ic_launcher.png` / `ic_launcher_round.png`.
