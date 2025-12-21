package com.example.androidcrud.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getEntryById(id: Long): EntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: EntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<EntryEntity>)

    @Update
    suspend fun updateEntry(entry: EntryEntity)

    @Delete
    suspend fun deleteEntry(entry: EntryEntity)

    @Query("DELETE FROM entries")
    suspend fun deleteAll()

    @androidx.room.Transaction
    suspend fun replaceAll(entries: List<EntryEntity>) {
        deleteAll()
        insertAll(entries)
    }
}
