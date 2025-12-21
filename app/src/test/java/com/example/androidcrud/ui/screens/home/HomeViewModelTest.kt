package com.example.androidcrud.ui.screens.home

import app.cash.turbine.test
import com.example.androidcrud.MainDispatcherRule
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.EntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<EntryRepository>()

    @Test
    fun uiState_emitsSuccess_whenRepositoryReturnsData() = runTest {
        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        coEvery { repository.getAllEntries() } returns flowOf(listOf(testEntry))

        val viewModel = HomeViewModel(repository)

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

        val viewModel = HomeViewModel(repository)

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
        val viewModel = HomeViewModel(repository)

        val testEntry = EntryEntity(id = 1, timestamp = Instant.now(), entryValue = 10)
        coEvery { repository.deleteEntry(testEntry) } returns Unit

        viewModel.deleteEntry(testEntry)

        coVerify { repository.deleteEntry(testEntry) }
    }
}