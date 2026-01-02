# Intermediate Design: Android CRUD App

## Overview

This document represents the current architectural state of the Android CRUD application. It evolves the `INITIAL_DESIGN.md` by incorporating features and changes implemented during Phases 1 through 8 of the `IMPLEMENTATION_CHECKLIST.md`, including full CRUD capabilities, UI polishing, and comprehensive testing.

## Current Project Status

- **Phases Completed:** 1 through 8 (Setup, Data Layer, Navigation, Add, Home, Delete, Edit, Polish & Testing).
- **Current Focus:** Maintenance, optimization, and potential new features.

## Requirements & Features

- **Data Model:** Entries containing:
    - `id`: Auto-generated Long.
    - `timestamp`: User-selectable `Instant`.
    - `entryValue`: Positive integer.
- **CRUD Operations:**
    - **Create:** Add new entries with validation.
    - **Read:** List all entries sorted by database order.
    - **Update:** Edit existing entries (value and timestamp).
    - **Delete:** Remove entries via Swipe-to-Dismiss or Trash icon.
- **Import/Export (Data Backup):**
    - **Export:** Save all entries to a JSON file with metadata (versioning).
    - **Import:** Restore entries from a JSON file, replacing existing data after user confirmation.
    - **Versioning:** Support for Semantic Versioning to ensure backward compatibility and prevent importing incompatible future versions.
- **UI/UX:**
    - Material 3 Design with Edge-to-Edge support.
    - Human-readable timestamp formatting.
    - Intuitive CRUD interactions (FAB, Swipe, Icon buttons).
    - Confirmation dialogs for destructive actions (Import).
- **Quality Assurance:**
    - Unit tests for ViewModels and Repositories.
    - Instrumented E2E tests for core flows and Import/Export.
    - Robolectric tests for UI logic.
- **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern.
- **UI Framework:** Jetpack Compose.

## Project Structure

The project structure remains consistent with the initial design, with updates to specific components to support the full CRUD lifecycle and data backup.

```
androidCrud/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/androidcrud/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── Converters.kt
│   │   │   │   │   │   ├── EntryDao.kt         <-- Supports InsertAll, DeleteAll, CRUD
│   │   │   │   │   │   ├── EntryEntity.kt
│   │   │   │   │   │   └── Serializers.kt      <-- Custom JSON serializers
│   │   │   │   │   ├── model/
│   │   │   │   │   │   └── BackupData.kt       <-- Serializable backup schema
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── EntryRepository.kt  <-- Exposes CRUD and ReplaceAll
│   │   │   │   │   │   └── BackupRepository.kt <-- Handles JSON Import/Export logic
│   │   │   │   │   └── di/
│   │   │   │   │       └── DatabaseModule.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── AppNavigation.kt    <-- Type-Safe Routes with Arguments
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt   <-- List with Edit/Delete actions and Import/Export
│   │   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   │   ├── add/
│   │   │   │   │   │   │   ├── AddEntryScreen.kt <-- Used for both Add and Edit
│   │   │   │   │   │   │   └── AddEntryViewModel.kt <-- Handles Edit Mode logic
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── utils/
│   │   │   │   │   └── VersionUtils.kt      <-- SemVer parsing and comparison
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── AndroidCrudApp.kt
│   │   │   └── AndroidManifest.xml
```

## Data Layer Architecture

### EntryDao
Expanded to support full CRUD and bulk operations:
- `insertEntry(entry: EntryEntity)`
- `updateEntry(entry: EntryEntity)`: Updates existing row.
- `deleteEntry(entry: EntryEntity)`
- `getAllEntries(): Flow<List<EntryEntity>>`
- `getEntryById(id: Long): EntryEntity?`
- `deleteAll()`
- `insertAll(entries: List<EntryEntity>)`
- `replaceAll(entries: List<EntryEntity>)`: Transactional delete and insert.

### EntryRepository
Exposes clean API to ViewModels:
- `getAllEntries(): Flow<List<EntryEntity>>`
- `getEntryById(id: Long): EntryEntity?`
- `saveEntry(entry: EntryEntity)`: Handles both Insert (if new) and Update (if exists).
- `deleteEntry(entry: EntryEntity)`
- `replaceAllEntries(entries: List<EntryEntity>)`: Replaces all existing data in a transaction.

### BackupRepository
Handles data serialization and version validation:
- `exportData(outputStream: OutputStream)`: Serializes database to JSON.
- `importData(inputStream: InputStream)`: Deserializes JSON and validates version before calling `replaceAllEntries`.

## UI Layer & Architecture (MVVM)

### Navigation (Type-Safe)
Updated to support passing arguments for the Edit flow.

```kotlin
@Serializable
object HomeDestination

@Serializable
data class AddEntryDestination(
    val entryId: Long? = null // Optional ID triggers "Edit Mode"
)
```

### Home Feature (List)
- **Screen:** `HomeScreen`
- **Functionality:**
    - Displays list of entries.
    - **Swipe-to-Delete:** Dismissible items.
    - **Delete Action:** Trash can icon button per row.
    - **Edit Action:** Pencil icon button per row navigates to `AddEntryDestination(entryId = id)`.
    - **FAB:** Navigates to `AddEntryDestination(entryId = null)`.

### Add/Edit Feature
- **Screen:** `AddEntryScreen`
    - Reused for both creating and editing.
    - Title changes based on context ("New Entry" vs "Edit Entry").
- **ViewModel:** `AddEntryViewModel`
    - **Initialization:** Reads `entryId` from `SavedStateHandle`.
    - **Edit Mode:** If `entryId` is present, fetches data from Repository to pre-fill state.
    - **State:** `AddEntryUiState`
        - Holds input fields (`entryValueInput`, `selectedTimestamp`).
        - Holds validation state.
    - **Actions:**
        - `saveEntry()`: Dispatches Insert or Update based on mode.

## Tech Stack (Confirmed)
- **Kotlin:** 1.9.x+
- **Jetpack Compose:** Material 3, Edge-to-Edge.
- **Room:** SQLite abstraction.
- **Hilt:** Dependency Injection.
- **Navigation Compose:** Type-Safe Serialization.
- **Java Time:** `java.time.Instant` for timestamps.

## Verification and Quality

With the completion of Phase 8, the application has undergone:
1.  **UI Refinement:** Audited for Material 3 standards, proper padding, typography, and edge-to-edge behavior. Timestamps are formatted for readability.
2.  **Unit Testing:** ViewModels (`HomeViewModel`, `AddEntryViewModel`) and `EntryRepository` are covered by unit tests ensuring logic correctness.
3.  **Instrumented Testing:** Robolectric and AndroidTest suites verify both UI logic and End-to-End flows (Add -> Verify -> Edit -> Verify -> Delete -> Verify).
