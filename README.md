# Courier Companion AIO

Android helper app for Uber Eats + Maxymo workflows. It does **not** read private app data, memory, traffic, or internal files. It uses visible-screen OCR scaffolding, notifications, GPS, local history, Maxymo export import, and overlay recommendations.

## Included
- Notification listener for Uber/Maxymo visible notification text
- GPS learning service
- Floating overlay recommendation panel
- OCR service scaffold using ML Kit
- Room/SQLite database
- Offer parser and scoring engine
- Maxymo history importer for CSV / JSON / TXT
- GitHub Actions workflow to build an APK from a phone

## Phone-only APK build
1. Create a new GitHub repository.
2. Upload all files from this ZIP.
3. Open **Actions**.
4. Run **Build Android APK**.
5. Download the `CourierCompanion-debug-apk` artifact.
6. Install the APK on your Android phone.

## Permissions to enable
- Location: for heatmap/parking-zone learning.
- Notification access: to record offer/update notifications.
- Display over other apps: to show the floating recommendation overlay.
- Screen capture: Android will require explicit confirmation when real MediaProjection capture is activated.

## Maxymo import format
Supported fields are auto-detected when possible:
- timestamp
- restaurant/store
- pickupArea
- dropoffArea
- offerAmount / payout / amount
- distanceKm / distance
- estimatedMinutes / minutes
- accepted
- completed

CSV, JSON array, JSON object with `history` or `offers`, and plain text lines are supported.

## Important limitation
The ScreenCaptureService is scaffolded and wired, but real MediaProjection capture still needs the Activity result handshake connected for production use. Notification import, Maxymo history import, scoring, database, overlay, and GPS service are implemented.
