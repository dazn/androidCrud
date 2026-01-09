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
  - **Status**: ✅ Completed - field added at line 16

### Database Migration
- [x] Create `MIGRATION_1_2` object with `ALTER TABLE entries ADD COLUMN note TEXT DEFAULT NULL`
  - **File**: `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt`
  - **Status**: ✅ Completed - migration created at lines 15-19

- [x] Update `@Database` annotation version from 1 to 2
  - **File**: `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt`
  - **Status**: ✅ Completed - version updated to 2 at line 9

- [x] Add migration to `DatabaseModule.provideAppDatabase()`
  - **File**: `app/src/main/java/com/example/androidcrud/di/DatabaseModule.kt`
  - **Status**: ✅ Completed - migration added at line 26

### Repository Layer
- [x] Verify no changes needed to `EntryRepository` (passes through `EntryEntity` transparently)
  - **Status**: ✅ Verified - no changes required

### Backup/Import Compatibility
- [x] Verify `BackupRepository` configuration handles nullable note field automatically
  - **Status**: ✅ Verified - `encodeDefaults = true` and `ignoreUnknownKeys = true` handle this

---

## Section 2: Add/Edit Screen UI

### AddEntryViewModel State
- [x] Update `AddEntryUiState` data class to include `noteInput: String = ""`
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Add field to data class at lines 19-25
  - **Status**: ✅ Completed

### ViewModel Methods
- [x] Add `updateNote(input: String)` method
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Location**: After `updateTimestamp()` method (after line 76)
  - **Status**: ✅ Completed

- [x] Update `saveEntry()` to include note field with blank-to-null conversion
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Update `EntryEntity` construction at lines 85-89
  - **Status**: ✅ Completed

### SavedStateHandle Persistence
- [x] Initialize `noteInput` from SavedStateHandle in constructor
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Add to initialization at lines 35-38
  - **Status**: ✅ Completed

- [x] Update `init` block to restore note when editing existing entry
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
  - **Line**: Update init block at lines 41-58
  - **Status**: ✅ Completed

- [x] Persist note input changes to SavedStateHandle in `updateNote()`
  - **Status**: ✅ Completed

### AddEntryScreen Layout
- [x] Add `OutlinedTextField` for note between value field and timestamp selector
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryScreen.kt`
  - **Location**: After line 98 (between value field and timestamp selector)
  - **Status**: ✅ Completed
  - **Requirements**:
    - Label: `stringResource(R.string.label_note)`
    - Single line: `singleLine = true`
    - Full width: `modifier = Modifier.fillMaxWidth()`
    - Spacer of 16.dp after field

### String Resources
- [x] Add `<string name="label_note">Note</string>` to strings.xml
  - **File**: `app/src/main/res/values/strings.xml`
  - **Status**: ✅ Completed

---

## Section 3: Home Screen List Display

### EntryItem Composable
- [ ] Update `EntryItem` to conditionally display note in supporting content
  - **File**: `app/src/main/java/com/example/androidcrud/ui/screens/home/HomeScreen.kt`
  - **Line**: Update `EntryItem` composable at lines 296-328
  - **Status**: ❌ Not started
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
  - **Status**: ✅ Completed

- [x] Test `saveEntry()` includes note in saved entity
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`
  - **Status**: ✅ Completed

- [x] Test saving with blank note converts to null
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`
  - **Status**: ✅ Completed

- [x] Test editing existing entry restores note value
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`
  - **Status**: ✅ Completed

- [x] Test editing entry with null note initializes to empty string
  - **File**: `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`
  - **Status**: ✅ Completed

### Instrumented Tests - Database Migration
- [ ] Create `MigrationTest.kt` file
  - **File**: `app/src/androidTest/java/com/example/androidcrud/data/local/MigrationTest.kt` (new file)
  - **Status**: ❌ Not started

- [ ] Test migration from version 1 to 2 with existing entries
  - **Requirements**:
    - Create v1 database with sample entries
    - Run migration to v2
    - Verify no data loss
    - Verify existing entries have note = null
    - Verify other fields unchanged
    - Verify new entries can be inserted with notes

### Instrumented Tests - AddEntryScreen UI
- [ ] Test note field is visible
  - **Status**: ❌ Not started

- [ ] Test note field accepts input
  - **Status**: ❌ Not started

- [ ] Test saving entry with note persists correctly
  - **Status**: ❌ Not started

- [ ] Test saving entry without note works (note = null)
  - **Status**: ❌ Not started

- [ ] Test editing entry with note displays note correctly
  - **Status**: ❌ Not started

### Instrumented Tests - HomeScreen UI
- [ ] Test note displays when present
  - **Status**: ❌ Not started

- [ ] Test note is hidden when null
  - **Status**: ❌ Not started

- [ ] Test note is hidden when blank
  - **Status**: ❌ Not started

- [ ] Test long notes are truncated with ellipsis
  - **Status**: ❌ Not started

### Instrumented Tests - Backup/Import
- [ ] Test exporting data includes note field
  - **Status**: ❌ Not started

- [ ] Test importing old backup (without note) succeeds
  - **Status**: ❌ Not started

- [ ] Test importing new backup (with note) restores notes correctly
  - **Status**: ❌ Not started

---

## Section 5: Version & Build Configuration

### Version Bump
- [ ] Update `versionCode` from 5 to 6
  - **File**: `app/build.gradle.kts`
  - **Line**: 18
  - **Status**: ❌ Not started

- [ ] Update `versionName` from "0.5.0" to "0.6.0"
  - **File**: `app/build.gradle.kts`
  - **Line**: 19
  - **Status**: ❌ Not started

---

### Verification & Testing

### Automated Testing
- [x] Run unit tests: `./gradlew test`
  - **Status**: ⚠️ Passed Debug (`testDebugUnitTest`), Failed Release (`testReleaseUnitTest` environment issue)

- [ ] Run instrumented tests: `./gradlew connectedAndroidTest`
  - **Status**: ❌ Not run (No device/emulator available)

- [x] Run full build: `./gradlew clean build`
  - **Status**: ⚠️ Partial (Debug assembled, Release check failed)
