package com.example.androidcrud.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidcrud.data.local.EntryEntity
import com.example.androidcrud.ui.screens.home.HomeUiState
import com.example.androidcrud.ui.screens.home.HomeViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    onAddEntryClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntryClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HomeUiState.Empty -> {
                    Text(
                        text = "No entries yet.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HomeUiState.Success -> {
                    EntryList(
                        entries = state.entries,
                        onDelete = viewModel::deleteEntry
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryList(
    entries: List<EntryEntity>,
    onDelete: (EntryEntity) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = entries,
            key = { it.id }
        ) { entry ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        onDelete(entry)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    val color by animateColorAsState(
                        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            Color.Transparent, 
                        label = "color"
                    )
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                },
                enableDismissFromStartToEnd = false
            ) {
                 EntryItem(
                     entry = entry,
                     onDeleteClick = { onDelete(entry) }
                 )
            }
        }
    }
}

@Composable
fun EntryItem(
    entry: EntryEntity,
    onDeleteClick: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
    }

    ListItem(
        headlineContent = { Text(text = "Value: ${entry.entryValue}") },
        supportingContent = { Text(text = formatter.format(entry.timestamp)) },
        trailingContent = {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Entry"
                )
            }
        }
    )
    HorizontalDivider()
}