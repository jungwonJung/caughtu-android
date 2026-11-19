package com.se7team.caughtu.ui.snapshots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.se7team.caughtu.data.AuthRepository
import com.se7team.caughtu.data.SnapshotRepository
import com.se7team.caughtu.data.model.Snapshot
import com.se7team.caughtu.domain.SnapshotListLogic
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SnapshotListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val items: List<Snapshot> = emptyList(),
)

class SnapshotListViewModel(
    snapshots: SnapshotRepository,
    private val auth: AuthRepository,
) : ViewModel() {

    // WhileSubscribed: the Firestore listener runs only while the list is on screen (+5 s for rotation).
    val uiState: StateFlow<SnapshotListUiState> = snapshots.observeMySnapshots()
        .map { SnapshotListUiState(isLoading = false, items = SnapshotListLogic.sortNewestFirst(it)) }
        .catch { emit(SnapshotListUiState(isLoading = false, error = it.message ?: "Could not load snapshots")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SnapshotListUiState())

    fun logout() = auth.logout()
}
