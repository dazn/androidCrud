package com.example.androidcrud.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: EntryRepository
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

    fun deleteEntry(entry: EntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
}
