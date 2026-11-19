package com.se7team.caughtu.ui.snapshots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.se7team.caughtu.data.AuthRepository
import com.se7team.caughtu.data.SnapshotRepository
import com.se7team.caughtu.data.model.Snapshot
import com.se7team.caughtu.domain.SnapshotListLogic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SnapshotListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<Snapshot> = emptyList(),
    val pendingDelete: Snapshot? = null,   // shown in the confirmation dialog
    val message: String? = null,           // one-shot snackbar text
)

private data class Extras(val pendingDelete: Snapshot? = null, val message: String? = null)

class SnapshotListViewModel(
    private val snapshots: SnapshotRepository,
    private val auth: AuthRepository,
) : ViewModel() {

    private val extras = MutableStateFlow(Extras())

    private val loaded = snapshots.observeMySnapshots()
        .map { SnapshotListUiState(isLoading = false, items = SnapshotListLogic.sortNewestFirst(it)) }
        .catch { emit(SnapshotListUiState(isLoading = false, error = it.message ?: "Could not load snapshots")) }

    // WhileSubscribed: the Firestore listener runs only while the list is on screen (+5 s for rotation).
    val uiState: StateFlow<SnapshotListUiState> = combine(loaded, extras) { s, x ->
        s.copy(pendingDelete = x.pendingDelete, message = x.message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SnapshotListUiState())

    fun askDelete(item: Snapshot) = extras.update { it.copy(pendingDelete = item) }
    fun cancelDelete() = extras.update { it.copy(pendingDelete = null) }

    fun confirmDelete() {
        val item = extras.value.pendingDelete ?: return
        extras.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            val text = snapshots.deleteSnapshot(item.id).fold(
                onSuccess = { "Deleted \"${item.label}\"" },
                onFailure = { it.message ?: "Could not delete. Please try again" },
            )
            extras.update { it.copy(message = text) }
        }
    }

    fun messageShown() = extras.update { it.copy(message = null) }

    fun logout() = auth.logout()
}
