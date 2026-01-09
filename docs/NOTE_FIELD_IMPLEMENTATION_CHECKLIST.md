# Note Field Feature Implementation Checklist

## Overview

Implementation checklist for adding an optional `note` text field to entries in the Android CRUD app.

**Plan Document**: `NOTE_FIELD_PLAN.md`
**Target Version**: 0.6.0
**Started**: 2026-01-09

---

## Section 1: Database & Data Layer

### EntryEntity Schema Changes
- [x] Update `EntryEntity` data class to include nullable `note: String? = null` field
  - **File**: `app/src/main/java/com/example/androidcrud/data/local/EntryEntity.kt`

### Database Migration
- [x] Create `MIGRATION_1_2` object with `ALTER TABLE entries ADD COLUMN note TEXT DEFAULT NULL`
  - **File**: `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt`

- [x] Update `@Database` annotation version from 1 to 2
  - **File**: `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt`

- [x] Add migration to `DatabaseModule.provideAppDatabase()`
  - **File**: `app/src/main/java/com/example/androidcrud/di/DatabaseModule.kt`

### Repository Layer
- [x] Verify no changes needed to `EntryRepository` (passes through `EntryEntity` transparently)

### Backup/Import Compatibility
- [x] Verify `BackupRepository` configuration handles nullable note field automatically

---

## Section 2: Add/Edit Screen UI

### AddEntryViewModel State
- [x] Update `AddEntryUiState` data class to include `noteInput: String = ""`
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Add field to data class at lines 19-25

### ViewModel Methods
- [x] Add `updateNote(input: String)` method
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Location**: After `updateTimestamp()` method (after line 76)

- [x] Update `saveEntry()` to include note field with blank-to-null conversion
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Update `EntryEntity` construction at lines 85-89

### SavedStateHandle Persistence
- [x] Initialize `noteInput` from SavedStateHandle in constructor
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Add to initialization at lines 35-38

- [x] Update `init` block to restore note when editing existing entry
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Update init block at lines 41-58

- [x] Persist note input changes to SavedStateHandle in `updateNote()`

### AddEntryScreen Layout
- [x] Add `OutlinedTextField` for note between value field and timestamp selector
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryScreen.kt`
  - **Location**: After line 98 (between value field and timestamp selector)
  - **Requirements**:
    - Label: `stringResource(R.string.label_note)`
    - Single line: `singleLine = true`
    - Full width: `modifier = Modifier.fillMaxWidth()`
    - Spacer of 16.dp after field

### String Resources
- [x] Add `<string name="label_note">Note</string>` to strings.xml
  - **File**: `app/src/main/res/values/strings.xml`

---

## Section 3: Home Screen List Display

### EntryItem Composable
- [x] Update `EntryItem` to conditionally display note in supporting content
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/home/HomeScreen.kt`
  - **Line**: Update `EntryItem` composable at lines 296-328
  - **Requirements**:
    - Wrap supporting content in `Column`
    - Show note only if `entry.note?.isNotBlank() == true`
    - Note text: `maxLines = 1`, `overflow = TextOverflow.Ellipsis`
    - Note appears above timestamp
    - No label prefix for note

---

## Section 4: Testing Strategy

### Unit Tests - AddEntryViewModel
- [x] Test `updateNote()` updates state correctly
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`

- [x] Test `saveEntry()` includes note in saved entity
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`

- [x] Test saving with blank note converts to null
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`

- [x] Test editing existing entry restores note value
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`

- [x] Test editing entry with null note initializes to empty string
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`

### Instrumented Tests - Database Migration
- [x] Create `MigrationTest.kt` file
  - **File**: `app/src/androidTest/java/com/example/androidcrud/data/local/MigrationTest.kt` (new file)

- [x] Test migration from version 1 to 2 with existing entries
  - **Requirements**:
    - Create v1 database with sample entries
    - Run migration to v2
    - Verify no data loss
    - Verify existing entries have note = null
    - Verify other fields unchanged
    - Verify new entries can be inserted with notes

### Instrumented Tests - AddEntryScreen UI
- [x] Test note field is visible
  - **File**: `app/src/androidTest/java/com/example/androidcrud/ui/screens/NoteFeatureTest.kt`

- [x] Test note field accepts input

- [x] Test saving entry with note persists correctly

- [x] Test saving entry without note works (note = null)

- [x] Test editing entry with note displays note correctly

### Instrumented Tests - HomeScreen UI
- [x] Test note displays when present
  - **File**: `app/src/androidTest/java/com/example/androidcrud/ui/screens/NoteFeatureTest.kt`

- [x] Test note is hidden when null

- [x] Test note is hidden when blank

- [x] Test long notes are truncated with ellipsis

### Instrumented Tests - Backup/Import
- [x] Test exporting data includes note field
  - **File**: `app/src/androidTest/java/com/example/androidcrud/ImportExportIntegrationTest.kt`

- [x] Test importing old backup (without note) succeeds

- [x] Test importing new backup (with note) restores notes correctly

---

## Section 5: Version & Build Configuration

### Version Bump
- [x] Update `versionCode` from 5 to 6
  - **File**: `app/build.gradle.kts`
  - **Line**: 18

- [x] Update `versionName` from "0.5.0" to "0.6.0"
  - **File**: `app/build.gradle.kts`
  - **Line**: 19

---

### Verification & Testing

### Automated Testing
- [x] Run unit tests: `./gradlew test`

- [ ] Run instrumented tests: `./gradlew connectedAndroidTest`

- [x] Run full build: `./gradlew clean build`
