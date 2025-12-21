package com.example.androidcrud.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    onBackClick: () -> Unit,
    viewModel: AddEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isEntrySaved) {
        if (uiState.isEntrySaved) {
            onBackClick()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Entry") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.entryValueInput,
                onValueChange = viewModel::updateEntryValue,
                label = { Text("Entry Value (Positive Integer)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.entryValueError,
                supportingText = {
                    if (uiState.entryValueError) {
                        Text("Value must be a positive integer", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            TimestampSelector(
                timestamp = uiState.selectedTimestamp,
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::saveEntry,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.entryValueInt != null && uiState.entryValueInt!! > 0
            ) {
                Text("Save Entry")
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.selectedTimestamp.toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val currentZone = ZoneId.systemDefault()
                            val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            val localTime = uiState.selectedTimestamp.atZone(currentZone).toLocalTime()
                            val newTimestamp = LocalDateTime.of(localDate, localTime).atZone(currentZone).toInstant()
                            viewModel.updateTimestamp(newTimestamp)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val currentZone = ZoneId.systemDefault()
            val localTime = uiState.selectedTimestamp.atZone(currentZone).toLocalTime()
            val timePickerState = rememberTimePickerState(
                initialHour = localTime.hour,
                initialMinute = localTime.minute
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val localDate = uiState.selectedTimestamp.atZone(currentZone).toLocalDate()
                        val newTime = java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val newTimestamp = LocalDateTime.of(localDate, newTime).atZone(currentZone).toInstant()
                        viewModel.updateTimestamp(newTimestamp)
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}

@Composable
fun TimestampSelector(
    timestamp: Instant,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val localDateTime = timestamp.atZone(zoneId)
    
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    Column {
        Text(
            text = "Date & Time",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = localDateTime.format(dateFormatter),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateClick() },
                enabled = false, // Disable typing, but handle click on parent or overlay
                trailingIcon = {
                    IconButton(onClick = onDateClick) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            OutlinedTextField(
                value = localDateTime.format(timeFormatter),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .weight(0.8f)
                    .clickable { onTimeClick() },
                enabled = false,
                trailingIcon = {
                    IconButton(onClick = onTimeClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Select Time")
                    }
                },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}