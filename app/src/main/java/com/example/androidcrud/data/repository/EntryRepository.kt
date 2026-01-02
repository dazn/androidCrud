package com.example.androidcrud.data.repository

import com.example.androidcrud.data.local.EntryDao
import com.example.androidcrud.data.local.EntryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EntryRepository @Inject constructor(
    private val entryDao: EntryDao
) {
    open fun getAllEntries(): Flow<List<EntryEntity>> = entryDao.getAllEntries()

    open suspend fun getEntryById(id: Long): EntryEntity? = entryDao.getEntryById(id)

    open suspend fun insertEntry(entry: EntryEntity) {
        if (entry.entryValue > 0) {
            entryDao.insertEntry(entry)
        } else {
            throw IllegalArgumentException("entryValue must be a positive integer")
        }
    }

    open suspend fun updateEntry(entry: EntryEntity) {
        if (entry.entryValue > 0) {
            entryDao.updateEntry(entry)
        } else {
            throw IllegalArgumentException("entryValue must be a positive integer")
        }
    }

    open suspend fun deleteEntry(entry: EntryEntity) {
        entryDao.deleteEntry(entry)
    }

    open suspend fun replaceAllEntries(entries: List<EntryEntity>) {
        entryDao.replaceAll(entries)
    }
}
