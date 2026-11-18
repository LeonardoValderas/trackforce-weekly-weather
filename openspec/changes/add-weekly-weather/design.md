# Technical design

## Context

The repository has a single Kotlin Android application module with a Compose starter screen. The module declares min SDK 24, target SDK 36, and compile SDK 36.1. The checked-in wrapper points to Gradle 9.5.0 and the daemon toolchain requests JDK 21. Confirm a clean build with those settings before documenting prerequisites.

## Goals

- Make the latest successful forecast available immediately from local storage on launch.
- Keep Android permission handling, provider data, and persistence outside the domain model.
- Exercise repository and presentation behavior with deterministic JVM tests.

## Decisions

### Layering and design patterns

Keep the existing `app` module and organize the feature by layer. The `ui` package contains `components`, `presentation`, `screen`, and `theme`. The app-level `di` package holds Hilt bindings.

| Layer | Responsibility |
| --- | --- |
| UI | `MainActivity` hosts Compose. `WeeklyWeatherScreen` launches the permission contract, renders the screen state, and forwards refresh and retry actions. |
| Presentation | `WeeklyWeatherViewModel` coordinates initial load and refresh in `viewModelScope`. It exposes immutable `StateFlow<WeeklyWeatherUiState>` collected with lifecycle awareness. |
| Domain | `ObserveWeeklyForecastUseCase` and `RefreshWeeklyForecastUseCase` expose forecast operations through a `ForecastRepository` interface. Domain models contain the seven days, display location, forecast time zone, and successful fetch instant. |
| Data | `ForecastRepositoryImpl` coordinates `OpenMeteoForecastDataSource` and `ForecastLocalDataSource`. `AndroidLocationAdapter` implements a domain location interface used by `RefreshWeeklyForecastUseCase`. |

Use Hilt for constructor injection and bind interface implementations in the application graph. Scope the refresh use case, repository, HTTP client, and DataStore to the application so callers share one cache and one in-flight refresh.

| Kind | Pattern and planned code | Role |
| --- | --- | --- |
| Creational | Simple Factory in `ForecastTargetFactory.create` | Builds either a device target from a usable fix or the labeled default target when the fix is absent, so callers use one construction path. |
| Structural | Adapter in `AndroidLocationAdapter` | Converts Android location APIs and their results to the domain location contract. |
| Behavioral | Observer in `WeeklyWeatherViewModel.uiState` and the Compose collector | The screen receives updates when the state changes and subscribes according to its lifecycle. |

### Location resolution

Request `ACCESS_COARSE_LOCATION` at first use via the Activity Result API. Coarse location is sufficient for a daily forecast. Keep the permission request in the UI and pass its result to the ViewModel. Recheck current permission at launch and on retry so a Settings change takes effect. Save whether the permission prompt was attempted as a separate preference so process restarts respect a denial.

When granted, request a current fix in parallel from the enabled platform fused, network, and GPS providers. Use the first available fix within a shared 10-second timeout and cancel the other requests. This lets an emulator GPS fix work even when network location is unavailable. When permission is denied or no provider supplies a fix, use the sample coordinates in the assessment (`45.50, -73.57`) as **Montréal (default location)**. Label a device forecast **Device location**. Keep any existing snapshot labeled with the location it was fetched for until a new snapshot is committed.

### REST contract

Use Ktor Client with its Android engine to call Open-Meteo over HTTPS with the resolved coordinates. Request `GET /v1/forecast` with `forecast_days=7`, `timezone=auto`, `temperature_unit=celsius`, and `daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max`. The suggested endpoint needs no API key for this assessment. Add Android's `INTERNET` permission. [Open-Meteo forecast API](https://open-meteo.com/en/docs)

Read `daily.time` as location-local dates and retain the response time zone. Match the arrays by index. Require seven consecutive dates and seven usable weather codes, minima, and maxima. Use the date range returned by Open-Meteo so a request spanning midnight does not depend on the device's time zone or on when it finished. A missing precipitation value leaves that day's precipitation field absent. Surface an unknown numeric weather code as **Unknown conditions**. Treat response errors, incomplete required fields, and invalid dates as refresh failures before writing anything. Inject a clock for the fetch timestamp.

### Cache and refresh

Use a single typed DataStore record for the latest successful forecast. Store a versioned snapshot containing the location identity used for display, a forecast time zone ID, the seven daily entries, and the fetch instant in UTC. A bounded one-record store fits the read and replace access pattern. Exclude this replaceable snapshot from Android backup and device transfer.

1. On initial screen load, start observing the persisted snapshot and display it as soon as it is available, including while a permission request or refresh is in progress.
2. Resolve the target after the permission outcome is known. Serialize refreshes and coalesce repeat actions while one is running. Use a bounded HTTP call timeout and propagate coroutine cancellation.
3. Validate and map the entire response, then persist the snapshot in one DataStore update. Publish fresh content through the store's observable stream after the write succeeds.
4. If a refresh fails, expose an inline retryable error alongside any visible forecast. With an empty cache, replace the loading view with an unavailable state and Retry. Format the stored fetch instant as a date and local time in the user's locale and device time zone.

### Test strategy

| Unit test | Expected result |
| --- | --- |
| Refresh use case with permission denied or a timed-out fix | Resolves Montréal and skips the device location call when permission is denied. |
| Repository refresh succeeds | Writes one complete snapshot with the injected fetch time. A new repository reads the same snapshot from storage. |
| Repository gets a network error or malformed payload | Retains the old snapshot and timestamp. |
| ViewModel sees an initial fetch failure with no cache | Exposes the forecast-unavailable state and permits retry. |
| ViewModel sees a refresh failure with cached data | Preserves the forecast and exposes a nonblocking error. |

Use fakes and coroutine test dispatchers for JVM tests. Verify actual disk persistence and process restart behavior on an emulator using the steps in [tasks.md](tasks.md).

## Risks and trade-offs

- The last saved forecast can be from a previous location or contain dates that have passed. Display its original label, dates, and timestamp so the user can judge its relevance.
- Android may return a coarse fix after a delay or fail to return one indoors. The location timeout bounds that wait and leads to a labeled default forecast.
- A clean clone requires the SDK platform and a compatible Gradle, Kotlin, and Hilt combination. Resolve dependency versions in the existing version catalog and confirm the documented toolchain from a clean checkout.
