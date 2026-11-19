package com.se7team.caughtu.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FieldValue
import com.se7team.caughtu.data.model.Snapshot
import com.se7team.caughtu.data.model.SnapshotInput
import com.se7team.caughtu.data.model.SnapshotType
import com.google.firebase.Timestamp
import java.util.Date

// Pending server timestamps are estimated locally so a freshly written item never shows an empty date.
private fun DocumentSnapshot.millis(field: String): Long? =
    getTimestamp(field, ServerTimestampBehavior.ESTIMATE)?.toDate()?.time

internal fun DocumentSnapshot.toSnapshot() = Snapshot(
    id = id,
    ownerId = getString("ownerId").orEmpty(),
    label = getString("label").orEmpty(),
    note = getString("note").orEmpty(),
    typeId = getString("typeId").orEmpty(),
    typeName = getString("typeName").orEmpty(),
    exportedAt = millis("exportedAt") ?: 0L,
    followerCount = getLong("followerCount") ?: 0L,
    followingCount = getLong("followingCount") ?: 0L,
    notFollowingBackCount = getLong("notFollowingBackCount") ?: 0L,
    createdAt = millis("createdAt"),
    updatedAt = millis("updatedAt"),
)

internal fun DocumentSnapshot.toSnapshotType() = SnapshotType(
    id = id,
    name = getString("name").orEmpty(),
    intervalDays = getLong("intervalDays") ?: 0L,
    order = getLong("order") ?: 0L,
)

/** Fields the user controls. Used for both create and update. */
internal fun SnapshotInput.toFields(): Map<String, Any> = mapOf(
    "label" to label.trim(),
    "note" to note.trim(),
    "typeId" to typeId,
    "typeName" to typeName,
    "exportedAt" to Timestamp(Date(exportedAt)),
    "followerCount" to followerCount,
    "followingCount" to followingCount,
    "notFollowingBackCount" to notFollowingBackCount,
    "updatedAt" to FieldValue.serverTimestamp(),
)
