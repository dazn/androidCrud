package com.example.androidcrud.ui.screens.home

import app.cash.turbine.test
import com.example.androidcrud.MainDispatcherRule
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.EntryRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import com.example.androidcrud.data.repository.BackupRepository
import android.content.Context

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<EntryRepository>()
    private val backupRepository = mockk<BackupRepository>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun uiState_emitsSuccess_whenRepositoryReturnsData() = runTest {
        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        coEvery { repository.getAllEntries() } returns flowOf(listOf(testEntry))

        val viewModel = HomeViewModel(repository, backupRepository, context)

        viewModel.uiState.test {
            // First emission might be Loading or Success depending on how fast the flow is
            val item1 = awaitItem()
            if (item1 is HomeUiState.Loading) {
                 val item2 = awaitItem()
                 assertTrue(item2 is HomeUiState.Success)
                 assertEquals(listOf(testEntry), (item2 as HomeUiState.Success).entries)
            } else {
                 assertTrue(item1 is HomeUiState.Success)
                 assertEquals(listOf(testEntry), (item1 as HomeUiState.Success).entries)
            }
        }
    }

    @Test
    fun uiState_emitsEmpty_whenRepositoryReturnsEmptyList() = runTest {
        coEvery { repository.getAllEntries() } returns flowOf(emptyList())

        val viewModel = HomeViewModel(repository, backupRepository, context)

        viewModel.uiState.test {
            val item1 = awaitItem()
             if (item1 is HomeUiState.Loading) {
                 val item2 = awaitItem()
                 assertTrue(item2 is HomeUiState.Empty)
            } else {
                 assertTrue(item1 is HomeUiState.Empty)
            }
        }
    }

    @Test
    fun deleteEntry_callsRepositoryDelete() = runTest {
        // Setup flow to keep VM happy
        coEvery { repository.getAllEntries() } returns flowOf(emptyList())
        val viewModel = HomeViewModel(repository, backupRepository, context)

        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        coEvery { repository.deleteEntry(testEntry) } returns Unit

        viewModel.deleteEntry(testEntry)

        coVerify { repository.deleteEntry(testEntry) }
    }

    @Test
    fun importData_success() = runTest {
        coEvery { repository.getAllEntries() } returns flowOf(emptyList())
        val contentResolver = mockk<android.content.ContentResolver>()
        every { context.contentResolver } returns contentResolver
        
        val uri = mockk<android.net.Uri>()
        val inputStream = java.io.ByteArrayInputStream("{}".toByteArray())
        every { contentResolver.openInputStream(uri) } returns inputStream
        
        coEvery { backupRepository.importData(any()) } returns Unit
        
        val viewModel = HomeViewModel(repository, backupRepository, context)
        
        viewModel.importData(uri)
        
        viewModel.importExportState.test {
            val item = awaitItem()
            if (item is ImportExportState.Loading) {
                 assertEquals(ImportExportState.Success("Import successful"), awaitItem())
            } else {
                 assertEquals(ImportExportState.Success("Import successful"), item)
            }
        }
    }

    @Test
    fun importData_error() = runTest {
        coEvery { repository.getAllEntries() } returns flowOf(emptyList())
        val contentResolver = mockk<android.content.ContentResolver>()
        every { context.contentResolver } returns contentResolver
        
        val uri = mockk<android.net.Uri>()
        val inputStream = java.io.ByteArrayInputStream("{}".toByteArray())
        every { contentResolver.openInputStream(uri) } returns inputStream
        
        coEvery { backupRepository.importData(any()) } throws RuntimeException("Version Mismatch")
        
        val viewModel = HomeViewModel(repository, backupRepository, context)
        
        viewModel.importData(uri)
        
        viewModel.importExportState.test {
            val item = awaitItem()
            if (item is ImportExportState.Loading) {
                 val errorItem = awaitItem()
                 assertTrue(errorItem is ImportExportState.Error)
                 assertEquals("Version Mismatch", (errorItem as ImportExportState.Error).message)
            } else {
                 assertTrue(item is ImportExportState.Error)
                 assertEquals("Version Mismatch", (item as ImportExportState.Error).message)
            }
        }
    }
}