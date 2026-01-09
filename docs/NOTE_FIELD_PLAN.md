# Note Field Feature Implementation Plan

## Overview

Add an optional `note` text field to entries in the Android CRUD app. The note field will:
- Be optional (entries can be created without notes)
- Appear in the add/edit screen as a single-line text field
- Display in the list screen between value and date, only when populated
- Have no character limit
- Display with ellipsis truncation if too long in list view

## Requirements Summary

- **Field Type**: Single-line text, no character limit
- **Storage**: Nullable String in database
- **Add/Edit UI**: OutlinedTextField labeled "Note"
- **List UI**: Show between value and timestamp, only if non-blank, truncate with ellipsis
- **Backward Compatibility**: Must work with existing data and backup files

---

## Design Section 1: Database & Data Layer

### EntryEntity Schema Changes

Update the `EntryEntity` data class to include the new optional note field:

```kotlin
@Serializable
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val entryValue: Int,
    val note: String? = null  // New optional field
)
```

**Location**: `app/src/main/java/com/example/androidcrud/data/local/EntryEntity.kt`

### Database Migration

Create a migration from version 1 to version 2:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE entries ADD COLUMN note TEXT DEFAULT NULL")
    }
}
```

**Location**: `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt` or a separate Migrations file

Update `AppDatabase` version:
```kotlin
@Database(entities = [EntryEntity::class], version = 2, exportSchema = false)
```

Update `DatabaseModule` to include the migration when building the Room database.

### Repository Layer

**No changes needed** to `EntryRepository` - it already passes through `EntryEntity` objects transparently.

### Backup/Import Compatibility

The existing `BackupRepository` configuration handles this automatically:

- **Export**: `encodeDefaults = true` means new backups will include the note field
- **Import old backups**: `ignoreUnknownKeys = true` means old backups without note field can be imported (note = null)
- **Import new backups to old app**: The note field will be ignored by older versions

**Files affected**:
- `app/src/main/java/com/example/androidcrud/data/local/EntryEntity.kt`
- `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt`
- `app/src/main/java/com/example/androidcrud/di/DatabaseModule.kt`

---

## Design Section 2: Add/Edit Screen UI

### AddEntryViewModel State

Update `AddEntryUiState` to track note input:

```kotlin
data class AddEntryUiState(
    val entryValueInput: String = "",
    val selectedTimestamp: Instant = Instant.now(),
    val entryValueInt: Int? = null,
    val entryValueError: Boolean = false,
    val isEntrySaved: Boolean = false,
    val noteInput: String = ""  // New field
)
```

### ViewModel Methods

Add new method:
```kotlin
fun updateNote(input: String) {
    savedStateHandle["noteInput"] = input
    _uiState.update { it.copy(noteInput = input) }
}
```

Update `saveEntry()` to include note:
```kotlin
val entry = EntryEntity(
    id = entryId ?: 0L,
    timestamp = currentState.selectedTimestamp,
    entryValue = value,
    note = currentState.noteInput.ifBlank { null }  // Convert blank to null
)
```

### SavedStateHandle Persistence

- Store `noteInput` in SavedStateHandle to survive process death
- When editing existing entry (entryId != null), restore note from loaded entry:
  ```kotlin
  savedStateHandle["noteInput"] = it.note ?: ""
  _uiState.update { currentState ->
      currentState.copy(
          entryValueInput = it.entryValue.toString(),
          selectedTimestamp = it.timestamp,
          entryValueInt = it.entryValue,
          noteInput = it.note ?: ""  // Restore note
      )
  }
  ```

### AddEntryScreen Layout

Add `OutlinedTextField` for note between the value field and timestamp selector:

```kotlin
OutlinedTextField(
    value = uiState.noteInput,
    onValueChange = viewModel::updateNote,
    label = { Text(stringResource(R.string.label_note)) },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true
)

Spacer(modifier = Modifier.height(16.dp))
```

**Field characteristics**:
- Single line input (`singleLine = true`)
- No validation or error states
- No character counter
- Uses default `KeyboardType.Text`

### String Resources

Add to `app/src/main/res/values/strings.xml`:

```xml
<string name="label_note">Note</string>
```

**Files affected**:
- `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt`
- `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryScreen.kt`
- `app/src/main/res/values/strings.xml`

---

## Design Section 3: Home Screen List Display

### EntryItem Composable

Update the `EntryItem` to conditionally display the note:

```kotlin
@Composable
fun EntryItem(
    entry: EntryEntity,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
    }

    ListItem(
        headlineContent = { Text(text = "Value: ${entry.entryValue}") },
        supportingContent = {
            Column {
                // Show note only if non-blank
                if (entry.note?.isNotBlank() == true) {
                    Text(
                        text = entry.note,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(text = formatter.format(entry.timestamp))
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Entry"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Entry",
                        tint = Color.Red
                    )
                }
            }
        }
    )
    HorizontalDivider()
}
```

**Display logic**:
- Only show note if `entry.note?.isNotBlank() == true` (handles null and empty strings)
- No label prefix for the note
- `maxLines = 1` with `TextOverflow.Ellipsis` for truncation
- Note appears above timestamp in the supporting content area

**Files affected**:
- `app/src/main/java/com/example/androidcrud/ui/screens/home/HomeScreen.kt`

---

## Design Section 4: Testing Strategy

### Unit Tests

**AddEntryViewModel** (`app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt`):

Follow the same testing patterns as existing `updateEntryValue` and `saveEntry` tests:

- Test `updateNote()` updates state correctly (similar to `updateEntryValue_updatesState_andValidatesInput`)
- Test `saveEntry()` includes note in saved entity (similar to `saveEntry_callsRepositoryInsert_whenNewEntry`)
- Test saving with blank note converts to null
- Test editing existing entry restores note value (similar to `init_loadsEntry_whenEditingExistingEntry`)
- Test editing entry with null note initializes to empty string

### Instrumented Tests

**AddEntryScreen UI Tests**:
- Verify note field is visible
- Verify note field accepts input
- Verify saving entry with note persists correctly
- Verify saving entry without note works (note = null)
- Verify editing entry with note displays note correctly

**HomeScreen UI Tests**:
- Verify note displays when present
- Verify note is hidden when null
- Verify note is hidden when blank
- Verify long notes are truncated with ellipsis

**Database Migration Test** (`app/src/androidTest/java/com/example/androidcrud/data/local/MigrationTest.kt` - create new file):

**IMPORTANT**: Test with a database that contains existing test entries (not empty database).

Test cases:
- Create version 1 database with sample entries (id, timestamp, entryValue)
- Run migration to version 2
- Verify migration succeeds without data loss
- Verify all existing entries have note = null after migration
- Verify existing entries' other fields (id, timestamp, entryValue) are unchanged
- Verify new entries can be inserted with notes
- Verify queries work correctly with nullable note field

Follow Room migration testing patterns (see Android Room documentation)

**Backup/Import Tests**:
- Test exporting data includes note field
- Test importing old backup (without note) succeeds
- Test importing new backup (with note) restores notes correctly

---

## Design Section 5: Implementation Steps & Considerations

### Implementation Order

1. **Database layer** (foundation):
   - Update `EntryEntity` with nullable `note` field
   - Create migration from version 1 to 2
   - Update `AppDatabase` version number to 2
   - Add migration to `DatabaseModule`

2. **ViewModel layer**:
   - Update `AddEntryUiState` with `noteInput` field
   - Add `updateNote()` method
   - Update `saveEntry()` to include note (convert blank to null)
   - Update init block to restore note when editing
   - Update SavedStateHandle initialization

3. **UI layer**:
   - Add note `OutlinedTextField` to `AddEntryScreen`
   - Update `EntryItem` composable to display note conditionally

4. **Testing**:
   - Add unit tests for ViewModel note handling
   - Add instrumented tests for UI behavior
   - Add migration test
   - Verify backup/restore functionality
   - Manual testing of edit flow

### Edge Cases Handled

- **Empty string notes**: Converted to null in `saveEntry()`, not displayed in list
- **Null notes**: Handled with safe call operator, not displayed in list
- **Process death**: Note input saved in SavedStateHandle
- **Old backups**: Import works, note = null for imported entries
- **New backups to old app**: Import works, note field ignored gracefully
- **Very long notes**: Truncated with ellipsis in list view
- **Editing entries without notes**: Initialize to empty string for better UX

### Version Bump

Update version in `build.gradle.kts`:
- From: `versionName = "0.5.0"` / `versionCode = 5`
- To: `versionName = "0.6.0"` / `versionCode = 6`

Rationale: Minor version bump for new feature with schema change.

---

## Files to Modify

### Data Layer
1. `app/src/main/java/com/example/androidcrud/data/local/EntryEntity.kt` - Add note field
2. `app/src/main/java/com/example/androidcrud/data/local/AppDatabase.kt` - Bump version, add migration
3. `app/src/main/java/com/example/androidcrud/di/DatabaseModule.kt` - Include migration

### UI Layer
4. `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryViewModel.kt` - Add note state and methods
5. `app/src/main/java/com/example/androidcrud/ui/screens/add/AddEntryScreen.kt` - Add note field
6. `app/src/main/java/com/example/androidcrud/ui/screens/home/HomeScreen.kt` - Display note in list

### Resources
7. `app/src/main/res/values/strings.xml` - Add note label string

### Build Configuration
8. `app/build.gradle.kts` - Bump version to 0.6.0 (code 6)

### Tests
9. `app/src/test/java/com/example/androidcrud/ui/screens/add/AddEntryViewModelTest.kt` - Add note-related unit tests
10. `app/src/androidTest/java/com/example/androidcrud/data/local/MigrationTest.kt` - Create migration tests (new file)
11. Optionally add instrumented UI tests for note field behavior (follow existing patterns)

---

## Success Criteria

- ✓ Entries can be created without notes (note = null)
- ✓ Entries can be created with notes
- ✓ Note field appears in add/edit screen
- ✓ Note displays in list only when populated
- ✓ Note displays between value and timestamp with no label
- ✓ Long notes truncate with ellipsis in list view
- ✓ Database migration succeeds without data loss
- ✓ Old backups can be imported
- ✓ New backups include notes
- ✓ All tests pass
- ✓ Version bumped to 0.6.0

---

## Verification Steps

After implementation, verify the feature works correctly:

### Automated Testing

1. **Run unit tests**: `./gradlew test`
   - Verify AddEntryViewModelTest passes
   - Verify all new note-related test cases pass

2. **Run instrumented tests**: `./gradlew connectedAndroidTest`
   - Verify MigrationTest passes (database migration with existing entries)
   - Verify backup/import integration tests pass

3. **Run full build**: `./gradlew clean build`
   - Verify no compilation errors
   - Verify type checking passes

## Technical Notes

### Room Database Migration
- Migration is additive and non-destructive
- `ALTER TABLE` with `DEFAULT NULL` ensures existing data is preserved
- KSP will regenerate DAO code automatically when EntryEntity changes

### Backup Compatibility
- JSON serialization with `ignoreUnknownKeys` ensures forward/backward compatibility
- No changes needed to `BackupRepository` or `BackupData` model
- `kotlinx.serialization` handles nullable fields automatically

### UI Considerations
- Single-line note field keeps add screen compact
- Label "Note" is simple and concise
- Ellipsis truncation in list prevents variable-height items
- Column layout in supporting content stacks note above timestamp
- Column is already imported in HomeScreen.kt (no additional import needed)

### Performance
- No performance impact: nullable String field is lightweight
- Indexed queries not needed (note is display-only, not queried)
- List rendering performance unchanged (same item count, fixed height)

---

**Plan created**: 2026-01-09
**Target version**: 0.6.0
**Estimated complexity**: Low-Medium
