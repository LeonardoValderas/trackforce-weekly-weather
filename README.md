# Trackforce Weekly Weather

A native Android app in Kotlin and Jetpack Compose. It shows seven daily forecasts for the device location, including conditions, low and high temperatures in Celsius, and precipitation probability when available. The last successful forecast remains available offline.

## Build and run

- Use Android Studio 2026.2 or a version that supports Android Gradle Plugin 9.3.3. [Android Studio and AGP compatibility](https://developer.android.com/build/releases/about-agp#android_gradle_plugin_and_android_studio_compatibility)
- Use JDK 21 for Gradle. The checked-in wrapper uses Gradle 9.5.0.
- Install Android SDK Platform 36.1, Android SDK Build-Tools 36.0.0, and Platform-Tools through SDK Manager. Use an emulator or device running Android 7.0 (API 24) or newer.
- Allow internet access for the first build to download Gradle and the project dependencies.

From a clean clone, open the directory containing `settings.gradle.kts` in Android Studio. Select JDK 21 under **Settings → Build, Execution, Deployment → Build Tools → Gradle**, sync the project, select an emulator or connected device, and run the **app** configuration.

For a command-line build, set `JAVA_HOME` to JDK 21 and `ANDROID_HOME` to the Android SDK directory, then run:

```sh
./gradlew :app:assembleDebug
./gradlew test
```

On Windows, use `gradlew.bat` in place of `./gradlew`. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The JVM tests cover location selection, Open-Meteo requests, disk persistence, failed refreshes, and ViewModel states.

## Location and weather provider

The app requests approximate location on first use. It checks the enabled Android location providers for up to 10 seconds. If permission is declined, location services are off, or the device supplies no fix in time, the app requests the forecast for **Montréal (default location)**. After a denial, grant permission from the app’s Android settings and tap **Refresh** or **Retry**.

On an emulator, use **Extended controls → Location** to send a location to the virtual device. On a physical device where GPS has trouble getting a fix, Android’s **Improve Location Accuracy** setting can make Wi-Fi and cellular location available. [Android Location Accuracy](https://support.google.com/android/answer/15157297)

The app sends the selected coordinates to Open-Meteo over HTTPS and requests seven days starting today in the forecast location’s time zone. The public forecast endpoint requires no API key for this assessment. [Weather data by Open-Meteo](https://open-meteo.com/en/docs)

## Emulator acceptance checks

These steps were verified on an API 32 emulator. Enable location services and internet access, then set a nondefault location, such as São Paulo, through **Extended controls → Location** before the first launch.

1. Launch the app and grant location permission. Verify **Device location**, all seven days, and the **Last updated** time.
2. Force-stop the app, disable Wi-Fi and mobile data, and relaunch it with app data intact. Verify the same forecast and timestamp.
3. Tap **Retry** while offline. The saved forecast stays visible with a nonblocking error. Clear the app’s storage, relaunch offline, and grant location permission at the prompt. Verify **No forecast available** and **Retry**. Restore connectivity and tap **Retry** to fetch the forecast. If the emulator lost its location fix, send the test location again.
4. With internet access restored, clear the app’s storage again and deny location permission on launch. Verify the Montréal forecast. Then grant location permission in Android app settings, turn off device Location, and relaunch to verify the same fallback with permission granted.

To force-stop from the command line, replace `emulator-5554` with the emulator serial shown by `adb devices`.

```sh
adb -s emulator-5554 shell am force-stop com.leonardo.valderas.meeklyweather
```

## Architecture and offline behavior

The app uses MVVM with a domain layer. The Compose route requests permission and collects the ViewModel’s `StateFlow` with lifecycle awareness. The ViewModel calls domain use cases, which depend on the `ForecastRepository` interface. Hilt binds the repository implementation and the platform location adapter. The repository uses Ktor for Open-Meteo and typed DataStore for local storage.

On launch, the screen observes DataStore while the app resolves location and fetches fresh data. The repository validates all seven days and atomically replaces one snapshot containing the forecast, its location, and the successful fetch timestamp. A failed refresh leaves that snapshot intact. With no saved snapshot, the screen shows the unavailable state.

## Design patterns

| Kind | Pattern and code | Why it fits |
| --- | --- | --- |
| Creational | Simple Factory in [ForecastTargetFactory](app/src/main/java/com/leonardo/valderas/meeklyweather/domain/model/ForecastTarget.kt) | Creates a forecast target from a device fix or the default city. |
| Structural | Adapter in [AndroidLocationAdapter](app/src/main/java/com/leonardo/valderas/meeklyweather/data/location/AndroidLocationAdapter.kt) | Converts Android provider responses to the domain’s `DeviceLocation` interface. |
| Behavioral | Observer in [WeeklyWeatherViewModel](app/src/main/java/com/leonardo/valderas/meeklyweather/ui/presentation/WeeklyWeatherViewModel.kt) and [WeeklyWeatherRoute](app/src/main/java/com/leonardo/valderas/meeklyweather/ui/screen/WeeklyWeatherRoute.kt) | Compose observes state changes while the screen is active. |

## With more time

- Add a city picker so users can choose a fallback when location is unavailable.
- Automate the permission and offline relaunch scenarios as Android UI tests in CI.
