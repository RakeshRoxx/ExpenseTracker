# Rupeeense Privacy Policy

Last updated: November 11, 2026

Rupeeense is an Android app for tracking personal expenses. This policy explains how the app handles SMS and transaction data.

## SMS access and consent

Rupeeense asks for your consent in the app before showing Android's SMS permission prompt. If you do not agree or deny the Android permission, SMS import stays off. You can still add transactions manually.

When you enable SMS import, Rupeeense reads SMS inbox messages on this device to identify bank, card, and UPI transaction alerts. It uses the sender, message date, and message text for on-device classification and transaction parsing. Messages that are not identified as financial transactions are not saved to Rupeeense's transaction database.

## Data stored on this device

For imported transactions, Rupeeense stores transaction details such as amount, merchant, transaction type, category, source, bank, and date, as well as the Android SMS message ID. It does not store the SMS message text in its transaction database.

When you long-press a transaction, Rupeeense uses the saved message ID to ask Android's SMS provider for the original message. If the message has been deleted or is otherwise unavailable, the app shows “No original text found.”

Manually added or edited transaction details and user-created categories are also stored in the app's local database. You can delete individual transactions in the app. Clearing Rupeeense's app storage or uninstalling it removes its local app data.

## On-device processing and sharing

SMS classification and transaction parsing run on the device. Rupeeense does not request Internet access and does not upload SMS content or transaction records to a Rupeeense server. The app no longer includes an SMS dataset export feature.

Android device backup and restore settings may back up app data. Those settings are controlled by Android and your Google account.

## Permissions

- `READ_SMS` is used only for the optional SMS transaction import feature described above.
- If SMS access is declined, manual transaction entry remains available.

## Children

Rupeeense is not directed to children under 13.

## Security

Rupeeense stores transaction records in the app's private local database and relies on Android's app sandbox. No app can guarantee absolute security. Protect your device and account, and avoid sharing screenshots that reveal financial details.

## Changes to this policy

This policy will be updated when the app's data practices change.

## Contact

For privacy or support questions, use the [Rupeeense project issue page](https://github.com/RakeshRoxx/ExpenseTracker/issues). Please do not include SMS text, bank details, or other private information in a public issue.
