# Weather at a Glance

An Android utility app for checking current conditions and the next few hours in Cairns, Queensland.

## Features

- Shows current temperature, apparent temperature, weather description, humidity, and wind speed.
- Shows a six-hour temperature outlook.
- Refreshes live data from the Open-Meteo Forecast API.
- Offers a Celsius/Fahrenheit setting that immediately updates the weather screen.
- Provides loading, retry, and refresh-failure feedback.
- Uses a simple bottom navigation bar to switch between Weather and Settings.

## Implementation

- Kotlin, Jetpack Compose, and Material Design 3.
- `WeatherRepository` owns the network request and JSON parsing. It calls Open-Meteo's public forecast endpoint for Cairns; no API key is required.
- `WeatherViewModel` loads data in `viewModelScope` and exposes immutable `StateFlow` UI state for lifecycle-aware collection.
- A small `ViewModelProvider.Factory` supplies the repository to the ViewModel (manual constructor injection).
- Temperature preference is held in ViewModel state and intentionally does not persist after the app process ends.
- The app requests only the Internet permission. It uses a fixed Cairns location and does not request location access.

## Run

Open this folder in Android Studio, allow Gradle sync, and run the `app` configuration on an Android 8.0 (API 26) or newer emulator/device. Internet access is needed for live weather.

## Data source

[Open-Meteo Forecast API](https://open-meteo.com/en/docs). Weather descriptions are mapped from the API's WMO weather codes. Wind speed is shown in km/h, as returned by the API by default.

## Assessment notes

This project is a coursework starting point and should be reviewed and understood by its student author before submission. The accompanying reflection is a draft: personalize the feelings and learning statements so they accurately represent your own experience. An AI-generated-material declaration is included separately.

GitHub repository: **Add your accessible GitHub repository URL here after publishing this project.** Share it with your lecturer and subject coordinator.
