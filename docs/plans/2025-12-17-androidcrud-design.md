# AndroidCrud App Design

**Date:** 2025-12-17
**Status:** Approved

## Overview

A simple Android CRUD application for logging entries with a timestamp and a positive integer value called "glue". The app prioritizes quick entry creation for tracking/logging over time.

## Requirements

- **Data Model:** Entries containing two fields:
  - Date/time (timestamp)
  - Glue (positive integer)
- **Primary Use Case:** Quick entry creation (logging/tracking)
- **Storage:** Local SQLite database using Room
- **UI Framework:** Jetpack Compose
- **Architecture:** Simple repository pattern
- **Dependency Injection:** Manual (constructor injection)
- **Testing:** Basic setup with sample tests

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
│   │   │   │   │   │   ├── EntryDao.kt
│   │   │   │   │   │   └── EntryEntity.kt
│   │   │   │   │   └── EntryRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── EntryListScreen.kt
│   │   │   │   │   │   └── AddEntryScreen.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── AndroidCrudApp.kt
│   │   │   └── AndroidManifest.xml
│   │   ├── test/ (unit tests)
│   │   └── androidTest/ (instrumented tests)
│   └── build.gradle.kts
├── gradle/
└── build.gradle.kts
```

## Technology Stack

### Core Dependencies
- **Kotlin:** 1.9.x with Kotlin DSL for Gradle
- **Jetpack Compose:** Latest stable BOM
- **Room:** 2.6.x for local database
- **Compose Navigation:** Screen navigation
- **ViewModel & StateFlow:** State management
- **Coroutines:** Async operations

### Testing Dependencies
- **JUnit 4:** Unit testing framework
- **Mockito:** Mocking framework
- **Compose UI Testing:** Instrumented UI tests
- **kotlinx-coroutines-test:** Testing coroutines

### Build Configuration
- **Minimum SDK:** 24 (Android 7.0 - 95%+ device coverage)
- **Target SDK:** 34 (Android 14)
- **Compile SDK:** 34
- **Gradle:** 8.x with version catalogs

## Data Layer Architecture

### Entry Entity
Simple data class with Room annotations:
- `id`: Auto-generated primary key (Long)
- `timestamp`: Date/time stored as Long (epoch milliseconds)
- `glue`: Positive integer (Int with validation)

### AppDatabase
Room database with single entity table using singleton pattern for single instance throughout app lifecycle.

### EntryDao (Data Access Object)
Provides database operations:
- `insertEntry()`: Add new entries (suspend function)
- `getAllEntries()`: Returns `Flow<List<EntryEntity>>` for reactive updates
- `getEntryById()`: Retrieve single entry
- `updateEntry()`: Modify existing entry
- `deleteEntry()`: Remove entry
- `deleteAllEntries()`: Clear all data

All operations are suspend functions for coroutine compatibility.

### EntryRepository
Clean API between UI and data layer:
- Wraps DAO operations
- Handles data mapping (Entity ↔ UI models if needed)
- Provides error handling and validation
- Returns Flow for reactive data streams
- Validates glue is positive before insertion

Passed to screens via constructor injection for easy testing with fake implementations.

## UI Layer & Navigation

### Screens

#### EntryListScreen (Home)
- LazyColumn displaying all entries sorted by timestamp (newest first)
- Each item shows: formatted date/time + glue value
- FloatingActionButton navigates to add entry screen
- Swipe-to-delete gesture on list items
- Empty state message when no entries exist

#### AddEntryScreen
- Date/time picker (defaults to current time)
- Number input field for glue value with validation
- Save button (disabled if glue is invalid)
- Cancel/back button
- Input validation: glue must be positive integer

### Navigation
Simple Compose Navigation with two destinations:
- `"list"` (start destination)
- `"add"`

Handled in MainActivity using NavHost.

### State Management
- `remember` and `mutableStateOf` for UI state
- Repository exposed via constructor for data operations
- Coroutines launched in `rememberCoroutineScope` for database operations
- `collectAsState()` to observe Flow from repository
- Direct screen-repository interaction (no ViewModels for simplicity)

## Testing Strategy

### Unit Tests (test/)
- `EntryRepositoryTest`: Test CRUD operations with in-memory Room database
- Validation tests: Ensure positive integer enforcement for glue field
- Use JUnit 4 and kotlinx-coroutines-test for testing suspend functions

### Instrumented Tests (androidTest/)
- `EntryDaoTest`: Verify Room database operations
- `EntryListScreenTest`: Test list display, empty state, navigation
- `AddEntryScreenTest`: Test input validation, save functionality
- Use Compose testing library (`createComposeRule()`)

### Test Approach
- Repository tests use real Room in-memory database (not mocked)
- UI tests use fake repository implementation for isolation
- Sample tests provided as templates for each layer

## Additional Details

### Error Handling
- Input validation in UI prevents negative/zero values for glue
- Database errors wrapped in Result/sealed class
- User-friendly error messages via Snackbar in Compose

### Date/Time Handling
- Store as Unix timestamp (Long) in database
- Format for display using SimpleDateFormat or java.time APIs
- Date/time picker defaults to "now" for quick entry

### Build System
- Gradle Kotlin DSL with version catalog (libs.versions.toml)
- Separate build configuration for debug/release
- ProGuard rules for release builds

## Implementation Notes

- Keep architecture simple - no over-engineering
- Prioritize quick entry UX in AddEntryScreen
- Ensure smooth list scrolling with LazyColumn
- Maintain reactive UI with Flow/StateFlow
- Follow Material Design 3 guidelines with Compose
