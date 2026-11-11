# Rupeeense Release Checklist

This folder contains release assets and configuration notes for Rupeeense.

## 1. SMS permission review

Rupeeense uses SMS for on-device transaction detection and budget tracking. The SMS-based money management use case is subject to Google Play review and approval.

Before submitting the app bundle:

- Complete Play Console's SMS permissions declaration for `READ_SMS`.
- Explain why automatic transaction extraction from financial SMS is a core feature and why there is no equivalent automatic source for those bank alerts.
- Ensure the Play listing prominently describes SMS-based transaction import.
- Provide review access and a short video showing the in-app disclosure, consent, Android permission prompt, decline path, and transaction tracking.
- Do not submit a build that contains SMS training export tools.

## 2. Privacy and data forms

- Publish `docs/privacy-policy.html` at the public HTTPS privacy policy URL.
- Add that URL to both the Play listing and an in-app entry point.
- Use the current Rupeeense name and a monitored support contact.
- Complete Data safety and Financial features declarations using the app's actual data flows and included SDKs.
- Recheck these declarations whenever permissions, SDKs, or data handling change.

## 3. Signing setup

1. Copy `keystore.properties.example` to `keystore.properties`.
2. Update all placeholder values.
3. Generate a keystore if you do not already have one:

```bash
mkdir -p release/keystore
keytool -genkeypair \
  -v \
  -keystore release/keystore/rupeeense-release.jks \
  -alias rupeeense \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Once `keystore.properties` exists, the Gradle release build uses it automatically.

## 4. Build signed APK

For device testing and GitHub releases:

```bash
./gradlew :app:renameReleaseApk
```

Expected output:

```text
app/build/renamed-apk/release/Rupeeense-v<versionName>-<versionCode>-release.apk
```

For Google Play, upload the signed Android App Bundle (`bundleRelease`) and complete the Play Console review declarations.

## 5. Screenshots

Use the checklist in `release/screenshots/README.md` and place final PNG or JPG images in `release/screenshots/`.

## 6. Final release package

Before publishing, confirm:

- release version code is higher than the previous Play release
- signed Android App Bundle for Google Play
- signed APK for direct testing or GitHub releases
- published privacy policy and in-app privacy link
- app icon, screenshots, and store listing copy
- Play Console SMS, Data safety, and Financial features declarations
