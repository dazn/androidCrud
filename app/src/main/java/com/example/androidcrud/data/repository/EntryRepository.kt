package com.example.androidcrud.data.repository

import com.example.androidcrud.data.local.EntryDao
import com.example.androidcrud.data.local.EntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(
    private val entryDao: EntryDao
) {
    fun getAllEntries(): Flow<List<EntryEntity>> = entryDao.getAllEntries()

    suspend fun getEntryById(id: Long): EntryEntity? = entryDao.getEntryById(id)

    suspend fun insertEntry(entry: EntryEntity) {
        if (entry.entryValue > 0) {
            entryDao.insertEntry(entry)
        } else {
            throw IllegalArgumentException("entryValue must be a positive integer")
        }
    }

    suspend fun deleteEntry(entry: EntryEntity) {
        entryDao.deleteEntry(entry)
    }
}
