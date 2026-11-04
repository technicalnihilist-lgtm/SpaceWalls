# Credits & Licensing

SpaceWalls bundles **no** images. Everything shown is fetched at runtime from two public archives,
and the credit block for each image travels with it into the app, the detail screen and the
in-app Credits tab.

## Sources

### Wikimedia Commons — https://commons.wikimedia.org/
Free-media repository operated by the Wikimedia Foundation. Files are contributed by photographers
and institutions under free licences or public domain. Each file page states the licence; the
Commons API exposes the same information as machine-readable `extmetadata`
(`Artist`, `Credit`, `LicenseShortName`, `LicenseUrl`, `UsageTerms`, `Restrictions`).

Attribution follows the Wikimedia reuse requirements: creator, licence name, licence URL and a
link to the original file page are displayed for every file.

### NASA Image and Video Library — https://images.nasa.gov/
Official NASA media library. NASA material is generally not subject to copyright in the United
States, subject to NASA's media usage guidelines
(https://www.nasa.gov/nasa-brand-center/images-and-media/). NASA does not endorse this app, and
individual items may embed third-party material whose rights sit with the original owner.

## Verification rules applied by the app

An image is offered as a wallpaper only if:

1. **Wikimedia** — a recognised public-domain/CC0 or CC BY / CC BY-SA licence URL is present, no
   usage restrictions are flagged, a creator is named (for attribution-required licences), the MIME
   type is JPEG/PNG/WebP, and the image is at least 1600×900.
2. **NASA** — the item names a NASA centre, the asset URL is served by NASA, no explicit copyright
   or stock-agency notice appears in the metadata, and the image is at least 1600×900. Partner
   credits (ESA, ESO, JAXA, STScI, AURA, CfA, …) are surfaced as an advisory on the detail screen.

Anything that cannot be verified is discarded before display. No credit line, creator or licence is
ever invented by the app.

## Third-party software

Kotlin, Jetpack Compose, Material 3, AndroidX (Room, WorkManager, DataStore, Navigation),
Retrofit, OkHttp, Gson, Coil — all under Apache License 2.0 (Coil: Apache 2.0).

## Reporting a credit problem

If an image in your installation is mis-credited or should not be offered, the detail screen links
directly to the original file page and licence, which is the authoritative record. Fixes belong at
the source (Commons file page or NASA record) and will flow into the app on the next fetch.
