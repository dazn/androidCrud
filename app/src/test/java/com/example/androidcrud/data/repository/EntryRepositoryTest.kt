package com.example.androidcrud.data.repository

import com.example.androidcrud.data.local.EntryDao
import com.example.androidcrud.data.local.EntryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class EntryRepositoryTest {

    private val dao = mockk<EntryDao>(relaxed = true)
    private val repository = EntryRepository(dao)

    @Test
    fun getAllEntries_delegatesToDao() {
        val testData = listOf(EntryEntity(1, Instant.now(), 10))
        coEvery { dao.getAllEntries() } returns flowOf(testData)

        val result = repository.getAllEntries()
        
        // simple verification it returns what dao returns
        // strictly speaking we should collect, but mocking flow return is enough to show connection
        // let's collect to be sure
        runTest {
            result.collect {
                assertEquals(testData, it)
            }
        }
    }

    @Test
    fun insertEntry_delegatesToDao() = runTest {
        val entry = EntryEntity(0, Instant.now(), 10)
        repository.insertEntry(entry)
        coVerify { dao.insertEntry(entry) }
    }

    @Test
    fun deleteEntry_delegatesToDao() = runTest {
        val entry = EntryEntity(1, Instant.now(), 10)
        repository.deleteEntry(entry)
        coVerify { dao.deleteEntry(entry) }
    }
    
    @Test
    fun updateEntry_delegatesToDao() = runTest {
        val entry = EntryEntity(1, Instant.now(), 20)
        repository.updateEntry(entry)
        coVerify { dao.updateEntry(entry) }
    }
}
