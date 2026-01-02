package com.example.androidcrud.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidcrud.R
import com.example.androidcrud.data.local.EntryEntity
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddEntryClick: () -> Unit,
    onEditEntryClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importExportState by viewModel.importExportState.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }

    // State for Import Confirmation
    var showImportConfirmationDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportConfirmationDialog = true
        }
    }

    // Effect for Import/Export State
    LaunchedEffect(importExportState) {
        when (val state = importExportState) {
            is ImportExportState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetImportExportState()
            }
            is ImportExportState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetImportExportState()
            }
            else -> {}
        }
    }

    if (showImportConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirmationDialog = false
                pendingImportUri = null
            },
            title = { Text(stringResource(R.string.import_dialog_title)) },
            text = { Text(stringResource(R.string.import_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri?.let { viewModel.importData(it) }
                        showImportConfirmationDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_continue),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportConfirmationDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_export_data)) },
                            onClick = {
                                showMenu = false
                                exportLauncher.launch("backup_${System.currentTimeMillis()}.json")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_import_data)) },
                            onClick = {
                                showMenu = false
                                importLauncher.launch(arrayOf("application/json"))
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntryClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (importExportState is ImportExportState.Loading) {
                 LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
            
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
                        onDelete = viewModel::deleteEntry,
                        onEdit = onEditEntryClick
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
    onDelete: (EntryEntity) -> Unit,
    onEdit: (Long) -> Unit
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
                     onDeleteClick = { onDelete(entry) },
                     onEditClick = { onEdit(entry.id) }
                 )
            }
        }
    }
}

@Composable
fun EntryItem(
    entry: EntryEntity,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault())
    }

    ListItem(
        headlineContent = { Text(text = "Value: ${entry.entryValue}") },
        supportingContent = { Text(text = formatter.format(entry.timestamp)) },
        trailingContent = {
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Entry"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Entry",
                        tint = Color.Red
                    )
                }
            }
        }
    )
    HorizontalDivider()
}
