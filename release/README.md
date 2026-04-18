# Release Checklist

This folder contains the assets and configuration notes needed for a public Rupee Radar release.

## 1. Privacy policy

- Markdown source: `docs/privacy-policy.md`
- Publishable page: `docs/privacy-policy.html`

Before publishing, replace the placeholder contact section with a real support email or website.

## 2. Signing setup

1. Copy `keystore.properties.example` to `keystore.properties`.
2. Update all placeholder values.
3. Generate a keystore if you do not already have one:

```bash
mkdir -p release/keystore
keytool -genkeypair \
  -v \
  -keystore release/keystore/rupee-radar-release.jks \
  -alias rupee-radar \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Once `keystore.properties` exists, the Gradle `release` build will use it automatically.

## 3. Build signed APK

Preferred command:

```bash
./gradlew renameReleaseApk
```

Expected output:

```text
app/build/renamed-apk/release/RupeeRadar-v<versionName>-<versionCode>-release.apk
```

If you sign outside Gradle, use the SDK `apksigner` binary from your Android SDK `build-tools` directory.

## 4. Screenshots

Use the checklist in `release/screenshots/README.md` and place final PNG or JPG images in `release/screenshots/`.

## 5. Final release bundle

Before publishing, make sure you have:

- signed APK
- privacy policy link
- app icon
- screenshots
- short app description
- version name and version code bumped
