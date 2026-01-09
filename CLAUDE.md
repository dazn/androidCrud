# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Native Android CRUD app built with Jetpack Compose. Manages numeric entries with timestamps, featuring local storage with Room database and JSON import/export functionality.

## Build Commands

```bash
# Build and check for errors
./gradlew build

# Run unit tests (Robolectric)
./gradlew test

# Run instrumented tests on connected device/emulator
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.example.androidcrud.ui.screens.home.HomeViewModelTest"

# Run specific test method
./gradlew test --tests "com.example.androidcrud.ui.screens.home.HomeViewModelTest.testDeleteEntry"

# Clean build
./gradlew clean
```

## Architecture

**MVVM with Repository pattern**. Three layers with clear separation:

### Data Layer
- **Room Database** (`AppDatabase`): Single source of truth for local persistence
- **EntryRepository**: Mediates between DAO and UI layer. Validates that `entryValue` must be positive integer
- **BackupRepository**: Handles JSON serialization for import/export with version compatibility checks via `VersionUtils`
- **Hilt DI**: All dependencies injected via `DatabaseModule` (Singleton scope)

### UI Layer
- **Jetpack Compose** with Material 3
- **Type-Safe Navigation**: Uses `@Serializable` destinations (`HomeDestination`, `AddEntryDestination`)
- **ViewModels**: Manage UI state via `StateFlow` and expose UI events

### Key Flow Patterns
- ViewModels collect from Repository's `Flow<List<EntryEntity>>` and transform to UI state
- Database operations happen on IO dispatcher (handled by Room and repositories)
- Import/export uses `InputStream`/`OutputStream` for file operations

## Testing Strategy

- **Unit tests** (`app/src/test`): Use Robolectric for Android components, MockK for mocking, Turbine for Flow testing
- **Instrumented tests** (`app/src/androidTest`): UI tests with Compose testing, E2E tests with real Room database
- **Custom test runner**: `CustomTestRunner` configured for Hilt in instrumented tests

## Dependencies

- **Build system**: Gradle with Kotlin DSL, KSP for annotation processing
- **DI**: Hilt (all repositories and DAOs injected)
- **Database**: Room with Flow-based reactive queries
- **Navigation**: Compose Navigation with kotlinx.serialization for type-safe args
- **Testing**: JUnit, MockK, Robolectric, Turbine, Compose UI Testing

## Code Organization

```
app/src/main/java/com/example/androidcrud/
├── data/
│   ├── local/          # EntryEntity, EntryDao, AppDatabase, Converters, Serializers
│   ├── model/          # BackupData, BackupMetadata
│   ├── repository/     # EntryRepository, BackupRepository
│   └── di/             # DatabaseModule (Hilt)
├── ui/
│   ├── navigation/     # Destinations (Serializable), AppNavigation
│   ├── screens/        # home/, add/ (each has Screen + ViewModel)
│   └── theme/          # Material 3 theme
├── utils/              # VersionUtils
└── MainActivity.kt     # Entry point with NavHost
```

## Version & Compatibility

- **Current version**: 0.5.0 (versionCode 5)
- **Min SDK**: 24, **Target SDK**: 36
- **Core library desugaring** enabled for java.time support on older Android versions
- Import/export validates backup file version compatibility against current `BuildConfig.VERSION_NAME`
