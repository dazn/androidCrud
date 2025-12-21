# Implementation Checklist: Android CRUD App

This checklist outlines the sequential steps to implement the Android CRUD application as defined in `INITIAL_DESIGN.md`.

## Phase 1: Project Setup & Infrastructure
- [x] **Initialize Gradle & Version Catalog:**
    - Set up `libs.versions.toml` with versions for Room, Hilt, Compose, Navigation, and KSP.
    - Configure root and app-level `build.gradle.kts` files.
- [x] **Configure Hilt:**
    - Create `AndroidCrudApp.kt` (inheriting from `Application` with `@HiltAndroidApp`).
    - Register the application in `AndroidManifest.xml`.
- [x] **Enable Edge-to-Edge:**
    - Call `enableEdgeToEdge()` in `MainActivity.onCreate`.
    - Set up basic Material 3 Theme in `ui/theme/`.

## Phase 2: Data Layer (Room & Repository)
- [x] **Define Data Model:**
    - Create `EntryEntity.kt` with `id`, `timestamp` (Instant), and `entryValue` (Int).
- [x] **Implement TypeConverters:**
    - Create `Converters.kt` for `Instant` <-> `Long` (epoch millis) conversion.
- [x] **Create DAO:**
    - Implement `EntryDao.kt` with CRUD operations (Insert, Delete, Get All as `Flow`, Get by ID).
- [x] **Setup Room Database:**
    - Create `AppDatabase.kt` and register `EntryEntity` and `Converters`.
- [x] **Hilt Database Module:**
    - Create `di/DatabaseModule.kt` to provide `AppDatabase`, `EntryDao`, and `EntryRepository`.
- [x] **Implement Repository:**
    - Create `EntryRepository.kt` to wrap DAO calls and handle basic business logic (e.g., entryValue > 0 validation).

## Phase 3: Navigation & UI Foundation
- [x] **Define Type-Safe Routes:**
    - Use `@Serializable` objects/classes for `HomeDestination` and `AddEntryDestination`.
- [x] **Implement Navigation Graph:**
    - Create `AppNavigation.kt` using `NavHost` and `composable` with type-safe arguments.

## Phase 4: Add Entry Feature
- [x] **Add Entry ViewModel:**
    - Implement `AddEntryViewModel.kt` using `SavedStateHandle`.
    - Define `AddEntryUiState` for entryValue input, date, and validation status.
- [x] **Add Entry Screen:**
    - Implement `AddEntryScreen.kt` with text fields and validation logic.
    - Handle `WindowInsets` using `Scaffold`.
- [x] **Timestamp Selection:**
    - Update `AddEntryViewModel` to support custom timestamp selection.
    - Implement Date & Time pickers in `AddEntryScreen`.
    - Format and display the selected timestamp in the UI.

## Phase 5: Home (List) Feature
- [x] **Home ViewModel:**
    - [x] Implement `HomeViewModel.kt` observing the repository's `Flow`.
    - [x] Define `HomeUiState` (Loading, Success, Empty, Error).
- [x] **Home Screen:**
    - [x] Implement `HomeScreen.kt` with a `LazyColumn` for entries.
    - [x] Add a Floating Action Button (FAB) for navigation to "Add Entry".
    - [x] Implement swipe-to-delete functionality.

## Phase 6: Delete Entry Feature
- [x] **Home Screen UI:**
    - [x] Add a Trash Can icon button to the right side of each entry row in `HomeScreen`.
    - [x] Connect the trash can button click to the delete functionality in `HomeViewModel`.

## Phase 7: Edit Entry Feature
- [x] **Update Data Layer:**
    - Add `updateEntry` method to `EntryDao` and `EntryRepository`.
- [x] **Edit Navigation:**
    - Update `AddEntryDestination` to accept an optional `entryId` argument.
- [x] **Edit Mode in ViewModel:**
    - Update `AddEntryViewModel` to load existing entry data if `entryId` is present.
    - Support updating existing entries in the database.
- [x] **Home Screen UI:**
    - Add a Pencil icon button to the right side of each entry row in `HomeScreen`.
    - Implement navigation to the edit screen upon clicking the pencil icon.

## Phase 8: Polish & Verification
- [x] **UI Refinement:**
    - Format timestamps for human readability in the list.
    - Ensure Material 3 design consistency and edge-to-edge compliance.
- [x] **Unit Testing:**
    - Write tests for `HomeViewModel` and `AddEntryViewModel`.
    - Write tests for `EntryRepository`.
- [x] **UI/Instrumented Testing:**
    - Create a Robolectric test for UI logic.
    - Create an E2E instrumented test (Start -> Add -> Verify).
