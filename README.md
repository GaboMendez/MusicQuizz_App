# 🎵 Music Quizz

A fun music knowledge quiz game for Android that challenges players to identify songs. Built as part of the Master's program in Advanced Software Technologies for Mobile Devices at Universidad San Jorge (USJ).

## 📱 Screenshots

| Splash Screen | Player Name | Game Screen | Results |
|:-------------:|:-----------:|:-----------:|:-------:|
| Loading songs | Enter name  | Quiz gameplay | Final score |

## ✨ Features

- **Splash Screen**: Downloads song data from the server while displaying a loading animation
- **Player Name Input**: Personalized experience with player names displayed throughout the app
- **Music Quiz Game**: 
  - Listen to song snippets and guess the correct answer
  - Multiple choice questions with 4 options
  - 15-second timer per question
  - Pause/Resume functionality
  - Save and continue game later
- **Scoring System**:
  - Base points for correct answers (1000 points)
  - Time bonus for faster answers (up to 500 points)
  - Streak bonus for consecutive correct answers (100 points per streak)
- **Results Screen**: View final score with percentage and option to play again
- **High Score Tracking**: Saves best scores locally

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Min SDK**: 33 (Android 13)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Activity-based with Application class for state management
- **UI**: View Binding + Material Design Components
- **Networking**: OpenAPI generated client (OkHttp)
- **Async**: Kotlin Coroutines
- **Media**: Android MediaPlayer API
- **Storage**: SharedPreferences for game state persistence

## 📂 Project Structure

```
musicquizz/
├── app/                          # Main application module
│   └── src/main/
│       ├── java/com/usj/musicquizz/
│       │   ├── MusicQuizzApp.kt      # Application class (global state)
│       │   ├── SplashActivity.kt     # Initial loading screen
│       │   ├── NameInputActivity.kt  # Player name entry
│       │   ├── GameActivity.kt       # Main quiz gameplay
│       │   └── ResultsActivity.kt    # Final score display
│       └── res/
│           ├── layout/               # Activity layouts
│           ├── drawable/             # Icons and graphics
│           ├── raw/                  # Audio files (if any)
│           └── values/               # Strings, colors, themes
│
├── api/                          # API client module
│   ├── client/                   # OpenAPI generated client
│   │   ├── api/openapi.yaml      # API specification
│   │   └── src/main/java/        # Generated API classes
│   └── src/main/java/            # API wrapper classes
│
└── build.gradle.kts              # Root build configuration
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or higher
- Docker Desktop (for running the backend server)

### Backend Server Setup

1. **Install Docker Desktop** following the [official guide](https://docs.docker.com/desktop/)

2. **Pull the music service image**:
   ```bash
   docker pull anselm82/music-service
   ```

3. **Run the server**:
   ```bash
   docker run -p 8080:8080 anselm82/music-service
   ```

4. **Verify** the server is running by visiting: http://localhost:8080

### Running the App

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/musicquizz.git
   cd musicquizz
   ```

2. **Open in Android Studio** and let Gradle sync

3. **Run the app** on an emulator or physical device

### ⚠️ Network Configuration

| Device Type | Server URL |
|-------------|------------|
| Android Emulator | `http://10.0.2.2:8080` |
| Physical Device | `http://<your-pc-ip>:8080` (e.g., `http://192.168.1.33:8080`) |

The app is pre-configured for emulator usage. For physical devices, update the base URL in `SplashActivity.kt`.

## 🎮 How to Play

1. **Launch the app** - Songs are automatically downloaded from the server
2. **Enter your name** - This will be used to personalize your experience
3. **Start playing** - Listen to the song snippet and select the correct answer
4. **Score points** - Answer quickly and build streaks for bonus points!
5. **View results** - See your final score and try to beat your high score

## 📡 API Endpoints

The app communicates with the Music Service API:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/MusicQuiz/api/songs` | GET | Retrieve all songs |
| `/MusicQuiz/api/songs/{id}` | GET | Get a specific song by ID |

### Song Model

```json
{
  "id": 1,
  "name": "Song Name",
  "author": "Artist Name",
  "file": "audio_file.mp3",
  "length": 180
}
```

## 🔧 Build & Configuration

### Regenerate API Client

If you need to update the API client after modifying `openapi.yaml`:

```bash
./gradlew openApiGenerate
```

### Build APK

```bash
./gradlew assembleDebug
```

The APK will be available at `app/build/outputs/apk/debug/app-debug.apk`

## 📋 Dependencies

```kotlin
// Core Android
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.10.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("androidx.gridlayout:gridlayout:1.0.0")

// API Module (OpenAPI generated client with OkHttp)
implementation(project(":api"))
```

## 📝 Assignment Requirements

This project fulfills the following requirements:

- ✅ Splash screen with song data preloading
- ✅ Player name input (no authentication required)
- ✅ Music quiz gameplay with multiple choice answers
- ✅ Scoring algorithm with time bonus and streak bonus
- ✅ Results display with personalized messages
- ✅ Docker-based backend integration

## 📄 License

This project is developed for educational purposes as part of the Master's program at Universidad San Jorge.

## 👨‍💻 Author

**Gabriel Méndez Reyes** - Master's Student at USJ

---

<p align="center">
  Made with ❤️ for the Mobile Development Course
</p>
