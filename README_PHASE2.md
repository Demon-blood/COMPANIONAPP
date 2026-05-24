# Courier Companion AIO — Phase 2 OCR Starter

This package is based on your successful Phase 1 V2 build.

## Added in Phase 2

- Screen capture permission flow with `MediaProjectionManager`
- `ScreenCapturePermissionActivity`
- foreground `ScreenCaptureService` that receives and owns the MediaProjection session
- OCR offer processing path:
  - visible text
  - offer detection
  - parser
  - score engine
  - Room database insert
  - overlay observes latest offers
- main screen button: **Start screen OCR monitor**

## Build

Upload the extracted contents to GitHub and run the included GitHub Actions workflow.

## Current status

This is a stable Phase 2 foundation. It intentionally stops before raw frame extraction to avoid breaking your working APK.
The next module will add:

- ImageReader
- VirtualDisplay
- bitmap extraction
- scheduled ML Kit OCR
- automatic overlay updates from live screenshots
