package com.se7team.caughtu.data.model

/**
 * One saved analysis of an Instagram export. Stored in the `snapshots` collection.
 * Only counts and the user's own label/note are stored – never usernames from the export.
 * Dates are epoch millis so the UI never needs a Firebase type.
 */
data class Snapshot(
    val id: String = "",
    val ownerId: String = "",             // set by the app from the auth session
    val label: String = "",
    val note: String = "",
    val typeId: String = "",              // id in `snapshotTypes`
    val typeName: String = "",            // copied for the list screen
    val exportedAt: Long = 0L,            // date of the Instagram export
    val followerCount: Long = 0L,
    val followingCount: Long = 0L,
    val notFollowingBackCount: Long = 0L,
    val createdAt: Long? = null,          // server timestamp
    val updatedAt: Long? = null,          // server timestamp
)

/** Read-only reference data from the `snapshotTypes` collection. */
data class SnapshotType(
    val id: String = "",
    val name: String = "",
    val intervalDays: Long = 0L,
    val order: Long = 0L,
)

/** What the user can edit. ownerId and timestamps are never part of the input. */
data class SnapshotInput(
    val label: String,
    val note: String,
    val typeId: String,
    val typeName: String,
    val exportedAt: Long,
    val followerCount: Long,
    val followingCount: Long,
    val notFollowingBackCount: Long,
)
