package com.se7team.caughtu.ui.snapshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.se7team.caughtu.data.model.SnapshotType
import com.se7team.caughtu.domain.SnapshotValidator
import com.se7team.caughtu.ui.auth.AuthTextField
import com.se7team.caughtu.ui.auth.LoadingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotEditScreen(viewModel: SnapshotEditViewModel, onDone: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.isDone) { if (state.isDone) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) "New snapshot" else "Edit snapshot") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.loadError != null -> Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.loadError!!, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { viewModel.load() }) { Text("Try again") }
                }
                else -> Form(state, viewModel)
            }
        }
    }
}

@Composable
private fun Form(state: SnapshotEditUiState, vm: SnapshotEditViewModel) {
    val f = state.form
    val e = state.errors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AuthTextField(f.label, vm::onLabelChange, "Label", e.label, KeyboardType.Text)
        TypePicker(state.types, f.typeId, e.typeId, vm::onTypeChange)
        AuthTextField(state.dateText, vm::onDateTextChange, "Export date (YYYY-MM-DD)", e.exportedAt, KeyboardType.Number)
        AuthTextField(f.followerCount, vm::onFollowersChange, "Followers", e.followerCount, KeyboardType.Number)
        AuthTextField(f.followingCount, vm::onFollowingChange, "Following", e.followingCount, KeyboardType.Number)
        AuthTextField(f.notFollowingBackCount, vm::onNotFollowingBackChange, "Don't follow you back", e.notFollowingBackCount, KeyboardType.Number)
        OutlinedTextField(
            value = f.note,
            onValueChange = vm::onNoteChange,
            label = { Text("Note (optional)") },
            isError = e.note != null,
            supportingText = { Text(e.note ?: "${f.note.trim().length} / ${SnapshotValidator.MAX_NOTE}") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        state.saveError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LoadingButton("Save", state.isSaving, vm::save)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypePicker(types: List<SnapshotType>, selectedId: String, error: String?, onSelect: (String) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = types.firstOrNull { it.id == selectedId }?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Snapshot type") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            types.forEach { type ->
                DropdownMenuItem(text = { Text(type.name) }, onClick = { onSelect(type.id); expanded = false })
            }
        }
    }
}
