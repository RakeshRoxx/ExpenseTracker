# Rupee Radar Privacy Policy

Last updated: April 11, 2026

Rupee Radar is an Android app that helps users identify and track transaction-related SMS messages on their device. This Privacy Policy explains what the app accesses, how data is handled, and what choices users have.

## Summary

- Rupee Radar reads SMS messages only after the user grants the `READ_SMS` permission.
- SMS parsing and transaction classification are performed on-device.
- The app does not request the `INTERNET` permission and does not send SMS content to a server.
- Transaction data is stored locally on the device using the app's local database.
- The app may show local notifications related to background SMS import.
- The app includes an export feature that can write a masked SMS dataset file to app-specific external storage when the user triggers that action.

## Information the App Accesses

Rupee Radar may access:

- SMS message body
- SMS sender/address
- SMS timestamp
- User-created or edited transaction records

This access is used to detect likely financial transactions, display them in the app, and let the user review or manage their records.

## How Data Is Used

The app uses the accessed data to:

- detect transaction-related SMS messages
- extract transaction details such as amount, merchant, bank, or type
- save transaction records locally on the device
- show transaction history and summaries
- schedule background imports and notifications, if enabled by the app

## Data Storage

- Transaction records are stored locally on the device in the app database.
- The app may remember the last processed SMS timestamp locally so it can avoid reprocessing the same messages.
- If the user uses the export feature, a masked dataset file may be created inside the app's external files directory on the device.

## Data Sharing

Rupee Radar does not sell personal data.

Based on the current app build, Rupee Radar does not send SMS content or transaction records to a remote server because the app does not request network access.

Data may still be accessible to:

- the user on their own device
- Android system backup/restore features, if enabled on the device
- third-party apps only if the user manually shares exported files outside the app

## Exported Files

If the user runs the export feature, the app creates a JSONL file containing masked SMS data for review or training-related workflows. The export process attempts to anonymize phone numbers, account references, UPI-like identifiers, and other long numeric values before writing the file.

Users are responsible for handling exported files carefully after export.

## Permissions

Rupee Radar may request:

- `READ_SMS` to read transaction-related SMS messages
- `POST_NOTIFICATIONS` to show notification updates related to background work
- foreground service permissions required by Android for background import behavior

If SMS permission is denied, some core app functionality will not work.

## Children

Rupee Radar is not directed to children under 13.

## Security

The app relies on Android's app sandbox and stores data locally. No app can guarantee absolute security, so users should keep their device protected and avoid sharing exported files unnecessarily.

## Changes to This Policy

This Privacy Policy may be updated when app behavior changes. The "Last updated" date will be revised when material changes are made.

## Contact

If you publish Rupee Radar publicly, replace this section with a real support email or website before release.
