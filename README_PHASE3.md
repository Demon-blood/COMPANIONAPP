# Courier Companion AIO — Phase 3 Live OCR

This build adds actual screen frame capture and live OCR processing.

## Added

- ImageReader-based screen frame capture
- VirtualDisplay connected to MediaProjection
- scheduled OCR every 5 seconds
- ML Kit OCR processing
- duplicate visible-offer filtering
- automatic parsed offer insert into Room database
- overlay updates from latest OCR/database result
- OCR runtime status shown in overlay

## Usage

1. Install APK.
2. Grant overlay permission.
3. Start overlay.
4. Tap **Start screen OCR monitor**.
5. Accept Android screen-capture permission.
6. Open Uber/Maxymo offer screen.
7. The app will OCR visible text, score detected offers, save them, and update the overlay.

## Notes

OCR quality depends on screen resolution, font size, language, and whether the offer card is fully visible.
Auto-clicking is not included; this app observes visible data and gives recommendations.
