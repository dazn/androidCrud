# Android CRUD App

A modern, native Android application built with Jetpack Compose that demonstrates best practices for local data management. The app allows users to log, track, and manage numeric entries with timestamps, featuring a complete CRUD (Create, Read, Update, Delete) lifecycle and data portability.

## Features

*   **Create Entries:** fast logging of positive integer values with customizable timestamps.
*   **View History:** Scrollable list of all entries, sorted by creation time, with human-readable date formatting.
*   **Edit & Update:** Modify existing entry values and timestamps.
*   **Delete:** Remove entries via swipe-to-dismiss gestures or a dedicated trash icon.
*   **Data Portability (Import/Export):**
    *   **Export:** Backup all data to a JSON file.
    *   **Import:** Restore data from a backup file (includes version validation and confirmation safety checks).
*   **Modern UI:** Fully Material 3 compliant with Edge-to-Edge design and support for dark/light themes.

## Architecture

This project follows a recommended **MVVM (Model-View-ViewModel)** architecture with a Repository pattern to ensure separation of concerns and testability.

### Key Components

*   **UI Layer:** Built entirely with Jetpack Compose
    *   Uses Type-Safe Navigation for routing between screens.
    *   ViewModels manage UI state (using `StateFlow`) and handle business logic.
*   **Data Layer:**
    *   **Room Database:** Local SQLite storage for persistence.
    *   **Repository:** Single source of truth that mediates between the database (DAO) and the UI.
    *   **BackupRepository:** Handles JSON serialization/deserialization and file I/O for import/export features.
*   **Dependency Injection:** Uses **Hilt** for managing dependencies across the app.

### Technology Stack

*   **Language:** Kotlin
*   **UI:** Jetpack Compose (Material 3)
*   **Persistence:** Room (SQLite) with KSP
*   **Navigation:** Compose Navigation (Type-Safe)
*   **Async:** Coroutines & Flow
*   **DI:** Hilt

## Project Structure

```
app/src/main/java/com/example/androidcrud/
├── data/
│   ├── local/          # Room Entity, DAO, Database, Converters
│   ├── model/          # Data models (e.g., BackupData schema)
│   ├── repository/     # EntryRepository, BackupRepository
│   └── di/             # Hilt Modules
├── ui/
│   ├── navigation/     # NavHost, Routes (Serializable)
│   ├── screens/        # Composable screens (Home, Add/Edit) & ViewModels
│   └── theme/          # Material 3 Theme definitions
```

## Getting Started

1.  **Clone the repository.**
2.  **Open in Android Studio** (Ladybug or newer recommended).
3.  **Sync Gradle** to download dependencies.
4.  **Run** on an Android Emulator or physical device (Min SDK 24).
