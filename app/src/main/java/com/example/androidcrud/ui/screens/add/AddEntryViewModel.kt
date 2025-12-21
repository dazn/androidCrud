package com.example.androidcrud.ui.screens.add

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.data.repository.EntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class AddEntryUiState(
    val entryValueInput: String = "",
    val selectedTimestamp: Instant = Instant.now(),
    val entryValueInt: Int? = null,
    val entryValueError: Boolean = false,
    val isEntrySaved: Boolean = false
)

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val entryRepository: EntryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState(
        entryValueInput = savedStateHandle["entryValueInput"] ?: "",
        selectedTimestamp = savedStateHandle["selectedTimestamp"] ?: Instant.now()
    ))
    val uiState: StateFlow<AddEntryUiState> = _uiState.asStateFlow()

    fun updateEntryValue(input: String) {
        savedStateHandle["entryValueInput"] = input
        val isValidInteger = input.toIntOrNull() != null && (input.toIntOrNull() ?: 0) > 0
        
        _uiState.update { currentState ->
            currentState.copy(
                entryValueInput = input,
                entryValueInt = input.toIntOrNull(),
                entryValueError = !isValidInteger && input.isNotEmpty() // Only show error if not empty and invalid
            )
        }
    }

    fun updateTimestamp(timestamp: Instant) {
        savedStateHandle["selectedTimestamp"] = timestamp
        _uiState.update { it.copy(selectedTimestamp = timestamp) }
    }

    fun saveEntry() {
        val currentState = _uiState.value
        val value = currentState.entryValueInt

        if (value != null && value > 0) {
            viewModelScope.launch {
                try {
                    entryRepository.insertEntry(
                        EntryEntity(
                            timestamp = currentState.selectedTimestamp,
                            entryValue = value
                        )
                    )
                    _uiState.update { it.copy(isEntrySaved = true) }
                } catch (e: Exception) {
                    // Handle error if needed
                }
            }
        } else {
             _uiState.update { it.copy(entryValueError = true) }
        }
    }
}
