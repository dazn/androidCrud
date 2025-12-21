# Intermediate Design: Android CRUD App

## Overview

This document represents the current architectural state of the Android CRUD application. It evolves the `INITIAL_DESIGN.md` by incorporating features and changes implemented during Phases 1 through 7 of the `IMPLEMENTATION_CHECKLIST.md`, specifically the addition of **Edit** and **Delete** capabilities, and the refinement of the **Add** flow.

## Current Project Status

- **Phases Completed:** 1 through 7 (Setup, Data Layer, Navigation, Add, Home, Delete, Edit).
- **Current Focus:** Phase 8 (UI Polish and Verification/Testing).

## Requirements & Features

- **Data Model:** Entries containing:
    - `id`: Auto-generated Long.
    - `timestamp`: User-selectable `Instant`.
    - `entryValue`: Positive integer.
- **CRUD Operations:**
    - **Create:** Add new entries with validation.
    - **Read:** List all entries sorted by database order (currently).
    - **Update:** Edit existing entries (value and timestamp).
    - **Delete:** Remove entries via Swipe-to-Dismiss or Trash icon.
- **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern.
- **UI Framework:** Jetpack Compose (Edge-to-Edge).

## Project Structure

The project structure remains consistent with the initial design, with updates to specific components to support the full CRUD lifecycle.

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
│   │   │   │   │   │   ├── EntryDao.kt         <-- Supports Insert, Update, Delete, Query
│   │   │   │   │   │   └── EntryEntity.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── EntryRepository.kt  <-- Exposes CRUD operations
│   │   │   │   │   └── di/
│   │   │   │   │       └── DatabaseModule.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   └── AppNavigation.kt    <-- Type-Safe Routes with Arguments
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt   <-- List with Edit/Delete actions
│   │   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   │   ├── add/
│   │   │   │   │   │   │   ├── AddEntryScreen.kt <-- Used for both Add and Edit
│   │   │   │   │   │   │   └── AddEntryViewModel.kt <-- Handles Edit Mode logic
│   │   │   │   │   ├── components/
│   │   │   │   │   └── theme/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   └── AndroidCrudApp.kt
│   │   │   └── AndroidManifest.xml
```

## Data Layer Architecture

### EntryDao
Expanded to support full CRUD:
- `insertEntry(entry: EntryEntity)`
- `updateEntry(entry: EntryEntity)`: Updates existing row.
- `deleteEntry(entry: EntryEntity)`
- `getAllEntries(): Flow<List<EntryEntity>>`
- `getEntryById(id: Long): EntryEntity?`

### EntryRepository
Exposes clean API to ViewModels:
- `getAllEntries(): Flow<List<Entry>>`
- `getEntryById(id: Long): Entry?`
- `saveEntry(entry: Entry)`: Handles both Insert (if new) and Update (if exists) logic implicitly or explicitly via ViewModel direction.
- `deleteEntry(entry: Entry)`

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

## Remaining Implementation Steps (Phase 8)

The core functionality is built. The following steps remain to reach the "Definition of Done":

1.  **UI Refinement:**
    - Improve timestamp formatting in `HomeScreen` (currently raw or basic formatting).
    - Audit UI against Material 3 standards (padding, typography).
    - Verify Edge-to-Edge behavior on different screen sizes/orientations.
2.  **Unit Testing:**
    - `HomeViewModel`: Test loading, empty states, delete events.
    - `AddEntryViewModel`: Test validation, save (insert vs update), state restoration.
    - `EntryRepository`: Test flow emissions and data mapping.
3.  **Instrumented Testing:**
    - Robolectric tests for local UI logic verification.
    - AndroidTest for End-to-End flows (Add -> Verify -> Edit -> Verify -> Delete -> Verify).
