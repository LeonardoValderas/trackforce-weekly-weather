# Add weekly weather to the Android app

## Why

Users need a seven-day forecast for their current location and access to the most recently saved forecast when their connection drops.

## What Changes

- Replace the starter screen with a native Kotlin forecast screen that loads on launch and offers manual refresh.
- Request foreground location permission and use Montréal when permission is denied or a location fix is unavailable.
- Store the most recent successful forecast on-device and display its last-updated time. Keep saved content visible through refresh failures.
- Add automated tests for location fallback, cache behavior, and screen states, plus clean-clone and emulator instructions for reviewers.

## Capabilities

### New Capabilities

- `weekly-weather` — A seven-day forecast with location resolution, offline access, and recoverable refresh errors.

### Modified Capabilities

## Impact

The existing Android app gains a forecast screen, a location adapter, an Open-Meteo client, and on-device storage. The Gradle version catalog and repository README gain the dependencies and instructions needed to build and review the feature.

The source requirements are the assessment package's `README.md` and `ACCEPTANCE_CRITERIA.md`.
