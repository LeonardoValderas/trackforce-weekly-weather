# Weekly weather

## Purpose

Show the forecast for the next seven local calendar days and preserve access to the last successful forecast across process restarts.

## ADDED Requirements

### Requirement: Forecast for the device location

The app SHALL fetch the weekly forecast for the resolved location when opened and when the user requests a refresh. It SHALL show seven daily entries in chronological order starting with today in the forecast location, each with a day, a weather description or icon, and labeled minimum and maximum temperatures.

#### Scenario: Online launch with an available location

- **GIVEN** the emulator has network access, location permission, and a configured location fix
- **WHEN** the user opens the app
- **THEN** the app fetches the forecast for that location and displays seven daily entries

#### Scenario: User refreshes a displayed forecast

- **GIVEN** a weekly forecast is visible
- **WHEN** the user selects Refresh and the request succeeds
- **THEN** the app displays the newly saved forecast and its updated timestamp

### Requirement: Foreground permission and location fallback

The app SHALL request foreground location permission before accessing device location. If permission is denied or a fix is unavailable, it SHALL request weather for Montréal when online and label the resulting forecast as the default location.

#### Scenario: Location permission is denied

- **WHEN** the user declines the location request while online
- **THEN** the app fetches and displays the Montréal forecast with a default-location label
- **AND** later launches respect the denial without prompting again automatically

#### Scenario: No usable device location

- **GIVEN** foreground location is authorized
- **WHEN** location services are disabled or a location fix cannot be obtained within the configured timeout
- **THEN** the app uses Montréal for the forecast request and displays the default-location label on success

### Requirement: Durable forecast across process death

The app SHALL save each complete successful forecast with its location label and last-updated time in durable local storage. On launch it SHALL load that snapshot for display while attempting a refresh.

#### Scenario: Offline relaunch after process death

- **GIVEN** an online launch successfully displayed a seven-day forecast
- **WHEN** the app process is killed and the user launches it again with network access disabled
- **THEN** the same seven entries, location label, and last-updated time are visible

### Requirement: Timestamp of displayed forecast

The app SHALL display the date and local time of the last successful update for the forecast currently on screen.

#### Scenario: Cached data is restored or refresh fails

- **GIVEN** the app previously saved a forecast
- **WHEN** the app reads that forecast from storage or a refresh fails
- **THEN** the displayed timestamp still reflects that forecast's original successful update

### Requirement: Offline launch with an empty cache

The app SHALL show an explicit forecast-unavailable state and a retry action if it has no saved forecast and the initial fetch fails.

#### Scenario: First launch without connectivity

- **GIVEN** app data is empty and network access is disabled
- **WHEN** the initial forecast fetch fails
- **THEN** the app stays open and explains that no forecast is available
- **AND** the user can retry when connectivity returns

### Requirement: Nonblocking refresh failure

If a refresh fails while a forecast is available, the app SHALL keep the saved forecast visible and show a nonblocking error with a retry action.

#### Scenario: Failed refresh with a cached forecast

- **GIVEN** the app displays a saved weekly forecast
- **WHEN** a refresh encounters a network, provider, response-validation, or storage error
- **THEN** the visible forecast, location label, and last-updated time stay the same
- **AND** the user sees a nonblocking refresh error and can retry
