package com.example.androidcrud.data.repository

import com.example.androidcrud.BuildConfig
import com.example.androidcrud.data.model.BackupData
import com.example.androidcrud.data.model.BackupMetadata
import com.example.androidcrud.utils.VersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    private val entryRepository: EntryRepository
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun exportData(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val entries = entryRepository.getAllEntries().first()
            val backupData = BackupData(
                metadata = BackupMetadata(
                    version = BuildConfig.VERSION_NAME,
                    timestamp = System.currentTimeMillis()
                ),
                entries = entries
            )
            
            val jsonString = json.encodeToString(BackupData.serializer(), backupData)
            outputStream.use { it.write(jsonString.toByteArray()) }
        }
    }

    suspend fun importData(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val jsonString = inputStream.use { it.bufferedReader().readText() }
            val backupData = json.decodeFromString(BackupData.serializer(), jsonString)
            
            VersionUtils.verifyVersionCompatibility(
                backupVersionString = backupData.metadata.version,
                currentVersionString = BuildConfig.VERSION_NAME
            )
            
            entryRepository.replaceAllEntries(backupData.entries)
        }
    }
}
