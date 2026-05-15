# Not Today, Sun! ☀️ Weather App
A modern Android weather application built with Kotlin that provides real-time weather updates, hourly forecasts, daily forecasts, favorite locations management, and weather notifications/alarms.

## Features:
🌍 Real-time weather based on GPS location
🗺️ Select locations from map
⭐ Save favorite locations
⏰ Weather alarms & notifications
📅 Hourly weather forecast
📆 Daily weather forecast
🌡️ Temperature unit conversion
🌐 Multi-language support
📡 Network connectivity handling
💾 Local database caching using Room
🔄 MVVM Architecture

## Tech Stack
Language: Kotlin
Architecture Pattern: MVVM
Database: Room Database
Networking: Retrofit
Location Services: Google Play Services Location API
Maps: OpenStreetMap (OSM)
Image Loading: Glide
UI Components: Android Jetpack Components
Notifications & Alarms: AlarmManager + BroadcastReceiver

## Project Structure
not_today_sun/
│
├── home/                 # Home screen and weather display
├── fav/                  # Favorite locations feature
├── notification/         # Alarm and notification handling
├── settings/             # App settings
├── model/
│   ├── local/            # Room database implementation
│   ├── remote/           # Retrofit API services
│   ├── pojo/             # Data models
│   └── repo/             # Repository layer
├── OSM/                  # OpenStreetMap integration
├── Splashscreen/         # Splash screen
├── utils/                # Utility/helper classes
└── key/                  # API key management

## Screens Included
- Home Screen
  Current weather
  Hourly forecast
  Daily forecast
- Favorites
  Save and manage favorite locations
- Notifications
  Create weather alerts and alarms
- Settings
  Language selection
  Unit preferences
  Location method selection

## Architecture Overview

- The project follows the MVVM Architecture:
View → Fragments & UI
ViewModel → Handles UI logic
Repository → Manages data operations
Local Data Source → Room Database
Remote Data Source → Retrofit API calls


### trello board: [Board](https://trello.com/b/AkTd4XIR/not-today-sun)
