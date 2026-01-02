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
- [x] **Create BackupRepository:**
    - Implement `BackupRepository` to handle the business logic of import/export.
    - **Export:** Fetch all entries -> Construct `BackupData` with `BuildConfig.VERSION_NAME` -> Serialize to JSON -> Write to `OutputStream`.
    - **Import:** Read `InputStream` -> Deserialize JSON -> Validate Version -> Call `entryRepository.replaceAllEntries`.
- [x] **Hilt Injection:**
    - Update `Di` modules to provide `BackupRepository`.

## Phase 4: UI & ViewModel Integration
- [x] **HomeViewModel Updates:**
    - Add `exportData(uri: Uri)` function.
    - Add `importData(uri: Uri)` function.
    - Add `ExportState` / `ImportState` (Idle, Loading, Success, Error) to `HomeUiState` or separate channel.
- [x] **Home Screen UI:**
    - Add a Top App Bar action menu (three dots) to `HomeScreen`.
    - Add "Export Data" and "Import Data" menu items.
    - Integrate `rememberLauncherForActivityResult`:
        - `CreateDocument` ("application/json") for Export.
        - `OpenDocument` ("application/json") for Import.
- [x] **User Feedback:**
    - Show `Snackbar` or `Toast` on success or error (specifically handling "Incompatible Version" errors).

## Phase 5: Confirm Data Import
- [x] **Confirmation Dialog:**
    - Implement a confirmation dialog triggered after selecting a backup file for import.
    - Message: "Restoration from backup file replaces all existing data. All existing data will be lost!"
    - Options:
        - **Cancel** (Gray): Dismisses the dialog and cancels the import process.
        - **Continue** (Red): Proceed with the import using the selected backup file.
- [x] **Logic Updates:**
    - Update `HomeScreen` to temporarily hold the selected URI until confirmation.
    - Call `viewModel.importData(uri)` only after "Continue" is clicked.

## Phase 6: Verification
- [x] **Unit Tests:**
    - Test `BackupRepository` logic (mocking Input/Output streams).
    - Verify version check throws exception for older major versions.
    - Verify JSON serialization matches expected format.
- [ ] **UI & Integration Tests:**
    - **Confirmation Dialog:** Verify that the confirmation dialog appears after selecting a file for import.
    - **Cancel Action:** Verify that clicking "Cancel" in the confirmation dialog dismisses it and does NOT trigger the import.
    - **Continue Action:** Verify that clicking "Continue" in the confirmation dialog dismisses it and triggers the import process.
    - **Full E2E Flow:** Create entries -> Export to file -> Clear database -> Import from file -> Verify entries are restored.
    - **Error Handling:** Verify appropriate snackbar messages are shown for version mismatch or corrupted files.
