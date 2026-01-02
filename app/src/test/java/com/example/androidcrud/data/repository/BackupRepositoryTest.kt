package com.example.androidcrud.data.repository

import com.example.androidcrud.BuildConfig
import com.example.androidcrud.MainDispatcherRule
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.model.BackupData
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant

class BackupRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val entryRepository = mockk<EntryRepository>(relaxed = true)
    private val backupRepository = BackupRepository(entryRepository)

    @Test
    fun exportData_writesJsonToStream() = runTest {
        val entry = EntryEntity(1, Instant.EPOCH, 123)
        every { entryRepository.getAllEntries() } answers { flowOf(listOf(entry)) }

        val outputStream = ByteArrayOutputStream()
        backupRepository.exportData(outputStream)

        val jsonString = outputStream.toString()
        // prettyPrint=true usually adds spaces
        assertTrue("Json should contain entryValue. Was: $jsonString", 
            jsonString.contains("\"entryValue\": 123") || jsonString.contains("\"entryValue\":123"))
        
        // Check for version presence
        assertTrue("Json should contain version. Was: $jsonString", jsonString.contains("\"version\":"))
    }

    @Test
    fun importData_parsesJsonAndCallsReplaceAll() = runTest {
        // Stub replaceAllEntries
        coEvery { entryRepository.replaceAllEntries(any()) } returns Unit
        
        // Use current version to ensure compatibility
        val currentVersion = BuildConfig.VERSION_NAME
        
        val json = """
            {
                "metadata": {
                    "version": "$currentVersion",
                    "timestamp": 123456789
                },
                "entries": [
                    {
                        "id": 1,
                        "timestamp": "2023-01-01T10:00:00Z",
                        "entryValue": 456
                    }
                ]
            }
        """.trimIndent()
        
        val inputStream = ByteArrayInputStream(json.toByteArray())
        
        backupRepository.importData(inputStream)
        
        val slot = slot<List<EntryEntity>>()
        coVerify { entryRepository.replaceAllEntries(capture(slot)) }
        
        assertEquals(1, slot.captured.size)
        assertEquals(456, slot.captured[0].entryValue)
    }

    @Test(expected = IllegalArgumentException::class)
    fun importData_throwsException_whenMajorVersionMismatch() = runTest {
        val currentVersion = BuildConfig.VERSION_NAME
        // If VERSION_NAME is not set or parseable, this might fail, but let's assume it is "1.0.0"
        val parts = currentVersion.split(".")
        if (parts.size >= 1) {
             val major = parts[0].toIntOrNull() ?: 1
             val mismatchVersion = "${major + 1}.0.0"
             
             val json = """
                {
                    "metadata": {
                        "version": "$mismatchVersion",
                        "timestamp": 123456789
                    },
                    "entries": []
                }
            """.trimIndent()
            
            val inputStream = ByteArrayInputStream(json.toByteArray())
            backupRepository.importData(inputStream)
        } else {
            // Fallback if version string is weird
             throw IllegalArgumentException("Forced Fail")
        }
    }
}