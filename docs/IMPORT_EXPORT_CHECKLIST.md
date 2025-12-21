# Implementation Checklist: Import/Export Features

This checklist outlines the sequential steps to implement data backup (export) and restore (import) functionality for the Android CRUD application.

## Phase 1: Data Models & Serialization
- [x] **Define Backup Schema:**
    - Create `BackupData.kt` annotated with `@Serializable`.
    - Include `metadata` object (containing `version` string, `timestamp` long) and `entries` list.
- [x] **Versioning Utility:**
    - Implement a utility to parse Semantic Versioning strings (Major.Minor.Patch).
    - Implement logic to check if `backupVersion.major < currentVersion.major`.

## Phase 2: Data Layer Extensions
- [x] **DAO Updates:**
    - Add `deleteAll()` method to `EntryDao`.
    - Add `insertAll(entries: List<EntryEntity>)` method to `EntryDao` (handling transactions).
- [x] **Repository Updates:**
    - Update `EntryRepository` to expose `replaceAllEntries(newEntries: List<EntryEntity>)`.

## Phase 3: Backup/Restore Logic
- [ ] **Create BackupRepository:**
    - Implement `BackupRepository` to handle the business logic of import/export.
    - **Export:** Fetch all entries -> Construct `BackupData` with `BuildConfig.VERSION_NAME` -> Serialize to JSON -> Write to `OutputStream`.
    - **Import:** Read `InputStream` -> Deserialize JSON -> Validate Version -> Call `entryRepository.replaceAllEntries`.
- [ ] **Hilt Injection:**
    - Update `Di` modules to provide `BackupRepository`.

## Phase 4: UI & ViewModel Integration
- [ ] **HomeViewModel Updates:**
    - Add `exportData(uri: Uri)` function.
    - Add `importData(uri: Uri)` function.
    - Add `ExportState` / `ImportState` (Idle, Loading, Success, Error) to `HomeUiState` or separate channel.
- [ ] **Home Screen UI:**
    - Add a Top App Bar action menu (three dots) to `HomeScreen`.
    - Add "Export Data" and "Import Data" menu items.
    - Integrate `rememberLauncherForActivityResult`:
        - `CreateDocument` ("application/json") for Export.
        - `OpenDocument` ("application/json") for Import.
- [ ] **User Feedback:**
    - Show `Snackbar` or `Toast` on success or error (specifically handling "Incompatible Version" errors).

## Phase 5: Verification
- [ ] **Unit Tests:**
    - Test `BackupRepository` logic (mocking Input/Output streams).
    - Verify version check throws exception for older major versions.
    - Verify JSON serialization matches expected format.
- [ ] **Integration Tests:**
    - Test the full flow: Create entries -> Export -> Delete entries -> Import -> Verify entries restored.
