# Rupeeense

Rupeeense is an Android personal expense tracker. It can classify transaction-related SMS messages on-device and organize detected transactions into a local expense timeline and monthly summary. SMS import is optional; manual transaction entry remains available without it.

## Features

- On-device classification and parsing of bank, card, and UPI transaction alerts
- Local transaction history and monthly summaries
- Manual transaction entry, editing, and categories
- Long-press a transaction to look up its original SMS while that message remains in the SMS inbox

## Download

- Latest release APK: https://github.com/RakeshRoxx/ExpenseTracker/releases/latest
- Project site: https://rakeshroxx.github.io/ExpenseTracker/
- Privacy policy: https://rakeshroxx.github.io/ExpenseTracker/privacy-policy.html

## SMS and privacy

Rupeeense shows an in-app disclosure before requesting Android's SMS permission. It reads inbox messages locally to identify financial transactions. For imported transactions, it stores the transaction details and SMS provider ID, not the message text. It does not request Internet access or upload SMS content.

See the privacy policy for the data details and choices.

## Release files

- Release checklist: `release/README.md`
- Screenshot checklist: `release/screenshots/README.md`
- Release notes draft: `release/release-notes-v1.0.0.md`
