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
- [ ] **Define Type-Safe Routes:**
    - Use `@Serializable` objects/classes for `HomeDestination` and `AddEntryDestination`.
- [ ] **Implement Navigation Graph:**
    - Create `AppNavigation.kt` using `NavHost` and `composable` with type-safe arguments.

## Phase 4: Add Entry Feature
- [ ] **Add Entry ViewModel:**
    - Implement `AddEntryViewModel.kt` using `SavedStateHandle`.
    - Define `AddEntryUiState` for entryValue input, date, and validation status.
- [ ] **Add Entry Screen:**
    - Implement `AddEntryScreen.kt` with text fields and validation logic.
    - Handle `WindowInsets` using `Scaffold`.

## Phase 5: Home (List) Feature
- [ ] **Home ViewModel:**
    - Implement `HomeViewModel.kt` observing the repository's `Flow`.
    - Define `HomeUiState` (Loading, Success, Empty, Error).
- [ ] **Home Screen:**
    - Implement `HomeScreen.kt` with a `LazyColumn` for entries.
    - Add a Floating Action Button (FAB) for navigation to "Add Entry".
    - Implement swipe-to-delete functionality.

## Phase 6: Polish & Verification
- [ ] **UI Refinement:**
    - Format timestamps for human readability in the list.
    - Ensure Material 3 design consistency and edge-to-edge compliance.
- [ ] **Unit Testing:**
    - Write tests for `HomeViewModel` and `AddEntryViewModel`.
    - Write tests for `EntryRepository`.
- [ ] **UI/Instrumented Testing:**
    - Create a Robolectric test for UI logic.
    - Create an E2E instrumented test (Start -> Add -> Verify).
