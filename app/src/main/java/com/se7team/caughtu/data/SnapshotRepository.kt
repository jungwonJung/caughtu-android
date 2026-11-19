package com.se7team.caughtu.data

import com.se7team.caughtu.data.model.Snapshot
import com.se7team.caughtu.data.model.SnapshotInput
import com.se7team.caughtu.data.model.SnapshotType
import kotlinx.coroutines.flow.Flow

/** The only entry point to snapshot data. ViewModels depend on this, never on Firebase. */
interface SnapshotRepository {
    /** Live list of the signed-in user's snapshots. Stops listening when collection stops. */
    fun observeMySnapshots(): Flow<List<Snapshot>>
    suspend fun getSnapshot(id: String): Result<Snapshot>
    suspend fun getTypes(): Result<List<SnapshotType>>
    suspend fun addSnapshot(input: SnapshotInput): Result<String>
    suspend fun updateSnapshot(id: String, input: SnapshotInput): Result<Unit>
    suspend fun deleteSnapshot(id: String): Result<Unit>
}
