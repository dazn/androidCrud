package com.example.androidcrud.ui.screens.add

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.example.androidcrud.MainDispatcherRule
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.EntryRepository
import com.example.androidcrud.ui.navigation.AddEntryDestination
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
class AddEntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<EntryRepository>(relaxed = true)

    @Test
    fun updateEntryValue_updatesState_andValidatesInput() {
        val savedStateHandle = SavedStateHandle(mapOf("entryId" to null))
        val viewModel = AddEntryViewModel(savedStateHandle, repository)

        viewModel.updateEntryValue("10")
        assertEquals("10", viewModel.uiState.value.entryValueInput)
        assertEquals(10, viewModel.uiState.value.entryValueInt)
        assertFalse(viewModel.uiState.value.entryValueError)

        viewModel.updateEntryValue("-5")
        assertTrue(viewModel.uiState.value.entryValueError)

        viewModel.updateEntryValue("abc")
        assertTrue(viewModel.uiState.value.entryValueError)
    }

    @Test
    fun saveEntry_callsRepositoryInsert_whenNewEntry() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("entryId" to null))
        val viewModel = AddEntryViewModel(savedStateHandle, repository)

        viewModel.updateEntryValue("42")
        viewModel.saveEntry()

        assertTrue(viewModel.uiState.value.isEntrySaved)
        coVerify { repository.insertEntry(any()) }
    }

    @Test
    fun init_loadsEntry_whenEditingExistingEntry() = runTest {
        val timestamp = Instant.now()
        val existingEntry = EntryEntity(id = 123L, timestamp = timestamp, entryValue = 99)
        
        coEvery { repository.getEntryById(123L) } returns existingEntry
        
        // Simulate navigation argument
        val savedStateHandle = SavedStateHandle(mapOf("entryId" to 123L))
        val viewModel = AddEntryViewModel(savedStateHandle, repository)

        // Wait for coroutine to finish (advanceUntilIdle is implicit in runTest usually, 
        // but since we're inside the init block which launches a coroutine, we rely on runTest dispatcher)
        
        // We might need to yield or let the test scheduler run.
        // With UnconfinedTestDispatcher (default in MainDispatcherRule), it should run immediately 
        // if it didn't suspend on something complex.
        
        // Check state
        assertEquals("99", viewModel.uiState.value.entryValueInput)
        assertEquals(timestamp, viewModel.uiState.value.selectedTimestamp)
    }

    @Test
    fun saveEntry_callsRepositoryUpdate_whenEditingEntry() = runTest {
        val existingEntry = EntryEntity(id = 123L, timestamp = Instant.now(), entryValue = 99)
        coEvery { repository.getEntryById(123L) } returns existingEntry
        
        val savedStateHandle = SavedStateHandle(mapOf("entryId" to 123L))
        val viewModel = AddEntryViewModel(savedStateHandle, repository)
        
        // Update value
        viewModel.updateEntryValue("100")
        viewModel.saveEntry()

        assertTrue(viewModel.uiState.value.isEntrySaved)
        coVerify { repository.updateEntry(match { it.id == 123L && it.entryValue == 100 }) }
    }
}
