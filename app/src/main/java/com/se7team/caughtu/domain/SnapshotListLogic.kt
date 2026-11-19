package com.se7team.caughtu.domain

import com.se7team.caughtu.data.model.Snapshot

/** Pure list logic for the snapshot list screen (unit-tested in Lab 3). */
object SnapshotListLogic {
    /** Newest export first; ties broken by creation time so the order is stable. */
    fun sortNewestFirst(items: List<Snapshot>): List<Snapshot> =
        items.sortedWith(compareByDescending<Snapshot> { it.exportedAt }.thenByDescending { it.createdAt ?: 0L })

    /** E1: case-insensitive search in label, note and type name. Blank query = everything. */
    fun filter(items: List<Snapshot>, query: String): List<Snapshot> {
        val q = query.trim()
        if (q.isEmpty()) return items
        return items.filter { s ->
            listOf(s.label, s.note, s.typeName).any { it.contains(q, ignoreCase = true) }
        }
    }
}
