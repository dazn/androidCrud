package com.example.androidcrud.ui.screens.home

import app.cash.turbine.test
import com.example.androidcrud.MainDispatcherRule
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.BackupRepository
import com.example.androidcrud.data.repository.EntryRepository
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import android.content.Context

class HomeViewModelDeletionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<EntryRepository>()
    private val backupRepository = mockk<BackupRepository>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun markForDeletion_removesEntryFromUiState() = runTest {
        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        val entriesFlow = MutableStateFlow(listOf(testEntry))
        coEvery { repository.getAllEntries() } returns entriesFlow

        val viewModel = HomeViewModel(repository, backupRepository, context)

        viewModel.uiState.test {
            // Initial state (Loading or Success)
            val initial = awaitItem()
            if (initial is HomeUiState.Loading) {
                 val success = awaitItem()
                 assertTrue(success is HomeUiState.Success)
            }

            // Mark for deletion
            viewModel.markForDeletion(testEntry)

            val emptyState = awaitItem()
            assertTrue(emptyState is HomeUiState.Empty)
        }
    }

    @Test
    fun cancelDeletion_restoresEntryToUiState() = runTest {
        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        val entriesFlow = MutableStateFlow(listOf(testEntry))
        coEvery { repository.getAllEntries() } returns entriesFlow

        val viewModel = HomeViewModel(repository, backupRepository, context)

        viewModel.uiState.test {
            val initial = awaitItem()
            if (initial is HomeUiState.Loading) awaitItem()

            // Mark for deletion
            viewModel.markForDeletion(testEntry)
            assertTrue(awaitItem() is HomeUiState.Empty)

            // Cancel deletion
            viewModel.cancelDeletion(testEntry)
            
            val restoredState = awaitItem()
            assertTrue(restoredState is HomeUiState.Success)
            assertEquals(listOf(testEntry), (restoredState as HomeUiState.Success).entries)
        }
    }

    @Test
    fun confirmDeletion_callsRepositoryDelete() = runTest {
        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        val entriesFlow = MutableStateFlow(listOf(testEntry))
        coEvery { repository.getAllEntries() } returns entriesFlow
        coEvery { repository.deleteEntry(testEntry) } returns Unit

        val viewModel = HomeViewModel(repository, backupRepository, context)

        // Trigger confirm
        viewModel.confirmDeletion(testEntry)

        coVerify { repository.deleteEntry(testEntry) }
    }
}
