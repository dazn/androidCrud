package com.example.androidcrud

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.BackupRepository
import com.example.androidcrud.data.repository.EntryRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ImportExportIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var entryRepository: EntryRepository

    @Inject
    lateinit var backupRepository: BackupRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun fullExportImportFlow() = runBlocking {
        // 1. Setup Initial Data
        val initialEntry1 = EntryEntity(timestamp = Instant.now(), entryValue = 101)
        val initialEntry2 = EntryEntity(timestamp = Instant.now(), entryValue = 202)
        
        // Clear DB first just in case
        entryRepository.replaceAllEntries(emptyList())
        
        entryRepository.saveEntry(initialEntry1)
        entryRepository.saveEntry(initialEntry2)
        
        // Verify insertion
        val currentEntries = entryRepository.getAllEntries().first()
        assertEquals(2, currentEntries.size)
        
        // 2. Export
        val outputStream = ByteArrayOutputStream()
        backupRepository.exportData(outputStream)
        val backupBytes = outputStream.toByteArray()
        
        assertTrue("Backup should not be empty", backupBytes.isNotEmpty())
        
        // 3. Clear Database (Simulate data loss or fresh install)
        entryRepository.replaceAllEntries(emptyList())
        assertEquals(0, entryRepository.getAllEntries().first().size)
        
        // 4. Import
        val inputStream = ByteArrayInputStream(backupBytes)
        backupRepository.importData(inputStream)
        
        // 5. Verify Restoration
        val restoredEntries = entryRepository.getAllEntries().first()
        assertEquals(2, restoredEntries.size)
        
        // Check values (Order might vary if not sorted by ID/Timestamp in getAllEntries, but usually it is)
        val values = restoredEntries.map { it.entryValue }.sorted()
        assertEquals(listOf(101, 202), values)
    }

    @Test
    fun exportImport_preservesNote() = runBlocking {
        // 1. Setup Data with Note
        val noteContent = "This is a backup note"
        val entryWithNote = EntryEntity(
            timestamp = Instant.now(), 
            entryValue = 303,
            note = noteContent
        )
        
        entryRepository.replaceAllEntries(emptyList())
        entryRepository.saveEntry(entryWithNote)
        
        // 2. Export
        val outputStream = ByteArrayOutputStream()
        backupRepository.exportData(outputStream)
        val backupBytes = outputStream.toByteArray()
        
        // 3. Clear DB
        entryRepository.replaceAllEntries(emptyList())
        
        // 4. Import
        val inputStream = ByteArrayInputStream(backupBytes)
        backupRepository.importData(inputStream)
        
        // 5. Verify Note
        val restoredEntries = entryRepository.getAllEntries().first()
        assertEquals(1, restoredEntries.size)
        assertEquals(noteContent, restoredEntries.first().note)
    }
}
