package com.example.androidcrud.ui.screens.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.BackupRepository
import com.example.androidcrud.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val entries: List<EntryEntity>) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface ImportExportState {
    data object Idle : ImportExportState
    data object Loading : ImportExportState
    data class Success(val message: String) : ImportExportState
    data class Error(val message: String) : ImportExportState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val backupRepository: BackupRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.getAllEntries()
        .map { entries ->
            if (entries.isEmpty()) {
                HomeUiState.Empty
            } else {
                HomeUiState.Success(entries)
            }
        }
        .catch { emit(HomeUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    private val _importExportState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val importExportState = _importExportState.asStateFlow()

    fun deleteEntry(entry: EntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    backupRepository.exportData(outputStream)
                } ?: throw IllegalStateException("Could not open output stream")
                _importExportState.value = ImportExportState.Success("Export successful")
            } catch (e: Exception) {
                _importExportState.value = ImportExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    backupRepository.importData(inputStream)
                } ?: throw IllegalStateException("Could not open input stream")
                _importExportState.value = ImportExportState.Success("Import successful")
            } catch (e: Exception) {
                _importExportState.value = ImportExportState.Error(e.message ?: "Import failed")
            }
        }
    }

    fun resetImportExportState() {
        _importExportState.value = ImportExportState.Idle
    }
}
