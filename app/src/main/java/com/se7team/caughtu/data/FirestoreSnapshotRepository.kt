package com.se7team.caughtu.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.se7team.caughtu.data.model.Snapshot
import com.se7team.caughtu.data.model.SnapshotInput
import com.se7team.caughtu.data.model.SnapshotType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSnapshotRepository(
    private val db: FirebaseFirestore = Firebase.firestore,
    private val auth: FirebaseAuth = Firebase.auth,
) : SnapshotRepository {

    private val snapshots get() = db.collection("snapshots")

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw DataException("You are signed out. Please log in again")

    override fun observeMySnapshots(): Flow<List<Snapshot>> = callbackFlow {
        // The query itself filters by owner – the rules would reject an unfiltered query anyway.
        val registration = snapshots
            .whereEqualTo("ownerId", requireUid())
            .addSnapshotListener { result, error ->
                if (error != null) {
                    close(DataException(friendlyDataMessage(error), error)); return@addSnapshotListener
                }
                if (result != null) trySend(result.documents.map { it.toSnapshot() })
            }
        awaitClose { registration.remove() } // stop listening when the screen is gone
    }

    override suspend fun getSnapshot(id: String): Result<Snapshot> = call {
        val doc = snapshots.document(id).get().await()
        if (!doc.exists()) throw DataException("This snapshot no longer exists")
        doc.toSnapshot()
    }

    override suspend fun getTypes(): Result<List<SnapshotType>> = call {
        db.collection("snapshotTypes").get().await()
            .documents.map { it.toSnapshotType() }
            .sortedBy { it.order }
    }

    override suspend fun addSnapshot(input: SnapshotInput): Result<String> = call {
        val fields = input.toFields() + mapOf(
            "ownerId" to requireUid(),
            "createdAt" to FieldValue.serverTimestamp(),
        )
        snapshots.add(fields).await().id
    }

    // update() only touches the given fields: ownerId and createdAt are never sent.
    override suspend fun updateSnapshot(id: String, input: SnapshotInput): Result<Unit> = call {
        snapshots.document(id).update(input.toFields()).await()
        Unit
    }

    override suspend fun deleteSnapshot(id: String): Result<Unit> = call {
        snapshots.document(id).delete().await()
        Unit
    }

    private inline fun <T> call(block: () -> T): Result<T> =
        runCatching(block).recoverCatching { e ->
            throw if (e is DataException) e else DataException(friendlyDataMessage(e), e)
        }
}
