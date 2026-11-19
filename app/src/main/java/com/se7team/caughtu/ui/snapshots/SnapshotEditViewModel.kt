package com.se7team.caughtu.ui.snapshots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.se7team.caughtu.data.SnapshotRepository
import com.se7team.caughtu.data.model.SnapshotInput
import com.se7team.caughtu.data.model.SnapshotType
import com.se7team.caughtu.domain.SnapshotErrors
import com.se7team.caughtu.domain.SnapshotForm
import com.se7team.caughtu.domain.SnapshotValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

data class SnapshotEditUiState(
    val isNew: Boolean = true,
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val types: List<SnapshotType> = emptyList(),
    val form: SnapshotForm = SnapshotForm(),
    val dateText: String = "",
    val errors: SnapshotErrors = SnapshotErrors(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isDone: Boolean = false,
)

/** id == null → create a new snapshot; otherwise load and update that one. */
class SnapshotEditViewModel(
    private val id: String?,
    private val repository: SnapshotRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SnapshotEditUiState(isNew = id == null))
    val uiState: StateFlow<SnapshotEditUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, loadError = null) }
        val types = repository.getTypes()
        val existing = id?.let { repository.getSnapshot(it) }
        val error = types.exceptionOrNull() ?: existing?.exceptionOrNull()
        if (error != null) {
            _uiState.update { it.copy(isLoading = false, loadError = error.message) }
            return@launch
        }
        val s = existing?.getOrNull()
        _uiState.update {
            it.copy(
                isLoading = false,
                types = types.getOrThrow(),
                form = if (s == null) it.form else SnapshotForm(
                    label = s.label, note = s.note, typeId = s.typeId, exportedAt = s.exportedAt,
                    followerCount = s.followerCount.toString(),
                    followingCount = s.followingCount.toString(),
                    notFollowingBackCount = s.notFollowingBackCount.toString(),
                ),
                dateText = s?.let { snap -> Instant.ofEpochMilli(snap.exportedAt).atZone(ZoneOffset.UTC).toLocalDate().toString() } ?: it.dateText,
            )
        }
    }

    fun onLabelChange(v: String) = _uiState.update { it.copy(form = it.form.copy(label = v), errors = it.errors.copy(label = null)) }
    fun onNoteChange(v: String) = _uiState.update { it.copy(form = it.form.copy(note = v), errors = it.errors.copy(note = null)) }
    fun onTypeChange(v: String) = _uiState.update { it.copy(form = it.form.copy(typeId = v), errors = it.errors.copy(typeId = null)) }
    fun onDateTextChange(v: String) = _uiState.update {
        it.copy(dateText = v, form = it.form.copy(exportedAt = SnapshotValidator.parseDate(v)), errors = it.errors.copy(exportedAt = null))
    }
    fun onFollowersChange(v: String) = _uiState.update { it.copy(form = it.form.copy(followerCount = v), errors = it.errors.copy(followerCount = null)) }
    fun onFollowingChange(v: String) = _uiState.update {
        it.copy(form = it.form.copy(followingCount = v), errors = it.errors.copy(followingCount = null, notFollowingBackCount = null))
    }
    fun onNotFollowingBackChange(v: String) = _uiState.update { it.copy(form = it.form.copy(notFollowingBackCount = v), errors = it.errors.copy(notFollowingBackCount = null)) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        val errors = SnapshotValidator.validate(s.form, now()).let {
            // A typed but unparseable date gets a clearer message than "required".
            if (s.form.exportedAt == null && s.dateText.isNotBlank()) it.copy(exportedAt = "Use the format YYYY-MM-DD") else it
        }
        _uiState.update { it.copy(errors = errors, saveError = null) }
        if (!errors.isValid) return // invalid input never reaches the backend

        val f = s.form
        val input = SnapshotInput(
            label = f.label, note = f.note, typeId = f.typeId,
            typeName = s.types.first { it.id == f.typeId }.name,
            exportedAt = f.exportedAt!!,
            followerCount = SnapshotValidator.parseCount(f.followerCount)!!,
            followingCount = SnapshotValidator.parseCount(f.followingCount)!!,
            notFollowingBackCount = SnapshotValidator.parseCount(f.notFollowingBackCount)!!,
        )
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (id == null) repository.addSnapshot(input).map { } else repository.updateSnapshot(id, input)
            _uiState.update {
                result.fold(
                    onSuccess = { _ -> it.copy(isSaving = false, isDone = true) },
                    onFailure = { e -> it.copy(isSaving = false, saveError = e.message ?: "Could not save. Please try again") },
                )
            }
        }
    }
}
