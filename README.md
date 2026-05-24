# Courier Companion AIO — Clean Phase 1

This is a clean, compile-focused Android/Kotlin base project for your Uber Eats + Maxymo companion app.

## Included

- Android Kotlin app
- AndroidX Activity/Core/Lifecycle
- Room database
- Coroutines
- Notification listener for visible notification text
- GPS logging foreground service
- Floating overlay scaffold
- ML Kit OCR engine hook
- Maxymo history import screen
- Offer parser + scoring engine
- GitHub Actions APK builder

## Phone-only APK build

1. Upload the extracted project contents to a GitHub repository.
2. Open the Actions tab.
3. Run **Build Android APK**.
4. Download the APK artifact.

## Important

This version is designed to compile cleanly first.
The screen capture service is a scaffold only; MediaProjection permission handshake will be added in the next phase.
