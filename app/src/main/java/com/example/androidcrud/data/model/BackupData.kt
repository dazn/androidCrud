package com.example.androidcrud.data.model

import com.example.androidcrud.data.local.EntryEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val metadata: BackupMetadata,
    val entries: List<EntryEntity>
)

@Serializable
data class BackupMetadata(
    val version: String,
    val timestamp: Long
)
