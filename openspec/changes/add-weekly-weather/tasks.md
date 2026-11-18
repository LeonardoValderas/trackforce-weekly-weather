# Implementation tasks

## 1. Build foundation

- [x] 1.1 Confirm the existing wrapper, JDK, SDK, and Android Gradle Plugin build from a clean checkout. Add compatible dependencies for Hilt, lifecycle-aware Compose state, coroutine testing, HTTP/JSON, and DataStore using the version catalog.
- [x] 1.2 Review ignore rules for generated build outputs, machine-local properties, keystores, and credentials.

## 2. Domain and data

- [x] 2.1 Define forecast models, the location and repository interfaces, and the observe and refresh use cases.
- [x] 2.2 Implement the foreground location adapter, bounded fix lookup, forecast target construction, and Montréal fallback.
- [x] 2.3 Implement the Ktor Open-Meteo request and response validation, weather code mapping, and atomic DataStore snapshot replacement.
- [x] 2.4 Connect the application graph with Hilt and configure Android manifest permissions and snapshot backup exclusions.

## 3. Forecast screen

- [x] 3.1 Replace the starter UI with seven-day forecast content, location label, last-updated date and time, and refresh and retry actions.
- [x] 3.2 Wire permission handling and ViewModel state for initial load, cached display, active refresh, nonblocking refresh failure, and empty-cache failure.

## 4. Verification and documentation

- [x] 4.1 Add at least three meaningful JVM tests using the use-case, repository, and ViewModel cases described in the design. Run `./gradlew test`.
- [x] 4.2 Set a nondefault emulator location and launch online. Grant location permission and verify seven entries, the device location label, and the last-updated time.
- [x] 4.3 Force-stop the app, disable emulator Wi-Fi and mobile data, and relaunch without clearing app data. Verify the same seven entries, location label, and timestamp.
- [x] 4.4 While offline with saved data visible, retry and verify the error appears alongside the unchanged forecast. Clear app data and relaunch offline to verify the unavailable state and retry action. Restore connectivity and verify recovery.
- [x] 4.5 With connectivity restored and permission reset, deny location access. Verify the labeled Montréal forecast. Grant permission in Settings and disable location services to check the unavailable-fix fallback independently.
- [x] 4.6 Document clean-clone run and test instructions with verified Android Studio, JDK, SDK, and Gradle prerequisites. Add emulator steps, architecture and cache behavior, fallback policy, Open-Meteo notes, the three pattern names and code locations with rationale, and potential follow-up work.
