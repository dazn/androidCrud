## Overview

A simple Android CRUD application for logging entries with a timestamp and a positive integer value called "entryValue". The app prioritizes quick entry creation for tracking/logging over time, adhering to modern Android development best practices (late 2024/2025 standards).

## Requirements

- **Data Model:** Entries containing two fields:
  - Date/time (timestamp) - User selectable during creation.
  - entryValue (positive integer)
- **Primary Use Case:** Quick entry creation (logging/tracking) with historical entry support.
- **Storage:** Local SQLite database using Room
- **UI Framework:** Jetpack Compose (Edge-to-Edge)
- **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern
- **Dependency Injection:** Hilt
- **Testing:** Unit tests (JUnit 4, Mockk, Robolectric), UI tests (Compose Test Rule)

## Project Structure

```
androidCrud/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/androidcrud/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── Converters.kt      <-- Added for TypeConverters
│   │   │   │   │   │   ├── EntryDao.kt
│   │   │   │   │   │   └── EntryEntity.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── EntryRepository.kt
│   │   │   │   │   └── di/
│   │   │   │   │       └── DatabaseModule.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── AppNavigation.kt (Type-Safe Routes)
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   │   ├── add/
│   │   │   │   │   │   │   ├── AddEntryScreen.kt
│   │   │   │   │   │   │   └── AddEntryViewModel.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── AndroidCrudApp.kt
│   │   │   └── AndroidManifest.xml
│   │   ├── test/ (unit tests & local UI tests)
│   │   └── androidTest/ (instrumented tests)
│   └── build.gradle.kts
├── gradle/
└── build.gradle.kts
```

## Technology Stack

### Core Dependencies
- **Kotlin:** 1.9.x+
- **Jetpack Compose:** Latest stable BOM
- **Room:** 2.6.x (with Coroutines/Flow support)
- **KSP (Kotlin Symbol Processing):** Replacing KAPT for Room/Hilt annotation processing.
- **Hilt:** Dependency Injection
- **Navigation Compose:** Type-Safe Navigation (Kotlin Serialization)
- **ViewModel & StateFlow:** State management
- **Lifecycle Runtime Compose:** `collectAsStateWithLifecycle`
- **Coroutines:** Async operations
- **Java Time (ThreeTenABP/Desugaring):** Modern date/time handling

### Testing Dependencies
- **JUnit 4:** Unit testing
- **Mockk:** Kotlin-first mocking
- **Robolectric:** Running Compose UI tests locally (JVM) for speed.
- **Hilt Testing:** For injecting dependencies in tests
- **Compose UI Testing:** Instrumented UI tests
- **kotlinx-coroutines-test:** Testing coroutines and flows

### Build Configuration
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)
- **Compile SDK:** 35
- **Gradle:** 8.x with Version Catalogs (`libs.versions.toml`)

## Data Layer Architecture

### Entry Entity
Simple data class with Room annotations:
- `id`: Auto-generated primary key (Long)
- `timestamp`: Stored as `Instant` directly (handled by TypeConverter).
- `entryValue`: Positive integer.

### Converters
- Maps `Instant` <-> `Long` (epoch millis) automatically for Room.

### AppDatabase
Room database. Uses `@TypeConverters`. Hilt provides the instance as a Singleton.

### EntryDao
- `insertEntry(entry: EntryEntity)`
- `getAllEntries(): Flow<List<EntryEntity>>`
- `deleteEntry(entry: EntryEntity)`
- `getEntryById(id: Long): EntryEntity?`

### EntryRepository
Single source of truth.
- Injected into ViewModels via Hilt.
- Exposes `Flow<List<Entry>>`.
- Handles business logic (e.g., ensuring `entryValue > 0` before calling DAO).

## UI Layer & Architecture (MVVM)

### UI Config
- **Edge-to-Edge:** Enabled in `MainActivity` (`enableEdgeToEdge()`).
- **Insets:** All screens use `Scaffold` and handle `WindowInsets` to avoid drawing behind system bars.

### State Management
UI State is modeled using **Sealed Interfaces** or **Data Classes**.
UI consumes state using **`collectAsStateWithLifecycle()`** to ensure flow collection stops when the app is backgrounded.

#### Home (List)
- **ViewModel:** `HomeViewModel`
- **State:** `HomeUiState`
  ```kotlin
  sealed interface HomeUiState {
      data object Loading : HomeUiState
      data class Success(val entries: List<Entry>) : HomeUiState
      data object Empty : HomeUiState
      data class Error(val message: String) : HomeUiState
  }
  ```
- **Events:** `deleteEntry(entry)`

#### Add Entry
- **ViewModel:** `AddEntryViewModel`
    - Uses `SavedStateHandle` to preserve input (entryValue value and selected timestamp) across process death.
- **State:** `AddEntryUiState`
  ```kotlin
  data class AddEntryUiState(
      val entryValueInput: String = "",
      val selectedTimestamp: Instant = Instant.now(),
      val entryValueInt: Int? = null,
      val entryValueError: Boolean = false,
      val isEntrySaved: Boolean = false
  )
  ```
- **Events:** `updateEntryValue(String)`, `updateTimestamp(Instant)`, `saveEntry()`

### Navigation
Uses **Type-Safe Navigation** (Compose Navigation 2.8.0+).

```kotlin
@Serializable
object HomeDestination

@Serializable
object AddEntryDestination
```

### Screens
- **HomeScreen:** Observes `HomeUiState`. Displays list or empty state. Floating Action Button navigates to `AddEntryDestination`.
- **AddEntryScreen:** Observes `AddEntryUiState`. Input fields. Validates "entryValue" is a positive integer.

## Testing Strategy

### Unit Tests (`test/`)
- **ViewModels:** Test state transitions and interaction with Repository (using `MainDispatcherRule`).
- **Repository:** Test logic and data flow using `app-cash-turbine` for Flow assertions.
- **Local UI Tests:** Use **Robolectric** to run Compose tests in the `test` source set. This avoids the overhead of the emulator for standard UI logic verification.

### Instrumented Tests (`androidTest/`)
- **End-to-End:** Test the full flow: Start App -> Verify List Empty -> Click Add -> Enter Data -> Save -> Verify List shows item.
- **Hilt:** Use `@HiltAndroidRule` to inject fake repositories for UI tests.

## Implementation Steps
1.  **Setup:** Initialize Gradle, Version Catalog, Hilt (w/ KSP), Room (w/ KSP), and Navigation dependencies.
2.  **Data Layer:** Create Entity, Converters, DAO, Database, and Repository. Set up Hilt module.
3.  **Domain:** Define models if separating from Entity (optional for simplicity).
4.  **UI - Add Entry:** Create ViewModel (w/ SavedStateHandle) and Screen. Implement validation.
5.  **UI - Home:** Create ViewModel and Screen. Implement List and Swipe-to-Delete.
6.  **Navigation:** Wire up screens using Type-Safe Navigation.
7.  **Polish:** Ensure Edge-to-Edge compliance (Insets), Theming, and formatting (Date/Time).
