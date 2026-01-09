package com.example.androidcrud.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
    val entryValue: Int,
    val note: String? = null
)
