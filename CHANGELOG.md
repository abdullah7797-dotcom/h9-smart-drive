# Changelog

## 1.3.0-beta.4 — 2026-11-27

- Expanded the GWM provider with tyre pressure and temperature, trip, fuel consumption, transmission temperature, steering angle, wheel speeds, torque, doors, drive mode and differential state.
- Added a live read-only Binder listener with a safe periodic snapshot fallback for the detected H9 SA8155 platform.
- Fixed Android Car API initialization on Android Automotive 9.
- Added per-signal availability to the privacy-limited compatibility report.
- Added GitHub update checks, an update icon, verified APK download, SHA-256 validation and signer validation before installation.

## 1.3.0-beta.3 — 2026-11-27

- Added a read-only Android Automotive property bridge for H9 2025 head units.
- Added a read-only fallback for the older GWM adapter service.
- Added BeanTechs, Car Service, TBOX, SOME/IP and cluster package diagnostics.
- Rebuilt the settings screen so compatibility details remain visible on 720px car displays.
- Added a privacy-limited copyable compatibility report and ADB collection tool.
- Fixed the header so it only says connected after receiving live vehicle data.
- Expire stale vehicle readings instead of leaving old values on screen.

## 1.3.0-beta.2 — 2026-11-26

- Redesigned the app for wide in-car landscape displays.
- Added a fixed side navigation rail and three-pane driving layout.
- Added redesigned driving, off-road, vehicle, apps and settings screens.
- Improved Arabic RTL layout, large touch targets and glanceable vehicle metrics.
- Kept GPS, demo data, diagnostics, native maps and media integrations.

## 1.3.0-beta.1 — 2026-11-24

- Initial Android 9+ project and reproducible Gradle build.
- Arabic/English landscape dashboard with light and dark themes.
- Demo, GPS, GWM probe, Android Automotive probe and OBD provider architecture.
- Vehicle, off-road, apps, settings and diagnostics screens.
- Room and DataStore persistence foundations.
- Unit tests for reading availability and staleness.
