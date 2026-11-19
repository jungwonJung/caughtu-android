package com.se7team.caughtu.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotValidatorTest {
    private val now = 1_800_000_000_000L
    private val valid = SnapshotForm(
        label = "June check", typeId = "weekly", exportedAt = now - 1,
        followerCount = "302", followingCount = "281", notFollowingBackCount = "42",
    )

    @Test fun validFormHasNoErrors() = assertTrue(SnapshotValidator.validate(valid, now).isValid)

    @Test fun labelRequiredAndMax40() {
        assertNotNull(SnapshotValidator.validate(valid.copy(label = "  "), now).label)
        assertNotNull(SnapshotValidator.validate(valid.copy(label = "x".repeat(41)), now).label)
    }

    @Test fun exportDateNotInFuture() =
        assertNotNull(SnapshotValidator.validate(valid.copy(exportedAt = now + 1), now).exportedAt)

    @Test fun countsMustBeWholeNonNegativeNumbers() {
        assertNotNull(SnapshotValidator.validate(valid.copy(followerCount = "-1"), now).followerCount)
        assertNotNull(SnapshotValidator.validate(valid.copy(followerCount = "1.5"), now).followerCount)
    }

    @Test fun notFollowingBackCannotExceedFollowing() {
        assertEquals("Can't be more than Following (281)",
            SnapshotValidator.validate(valid.copy(notFollowingBackCount = "282"), now).notFollowingBackCount)
        assertNull(SnapshotValidator.validate(valid.copy(notFollowingBackCount = "281"), now).notFollowingBackCount)
    }

    @Test fun noteMax200() =
        assertNotNull(SnapshotValidator.validate(valid.copy(note = "x".repeat(201)), now).note)
}

class SnapshotListLogicTest {
    private val a = com.se7team.caughtu.data.model.Snapshot(id = "a", label = "June check", note = "after cleanup", typeName = "Weekly check")
    private val b = com.se7team.caughtu.data.model.Snapshot(id = "b", label = "Before trip", note = "", typeName = "Monthly check")

    @Test fun searchIgnoresCaseAndCoversLabelAndNote() {
        assertEquals(listOf(a), SnapshotListLogic.filter(listOf(a, b), "JUNE"))
        assertEquals(listOf(a), SnapshotListLogic.filter(listOf(a, b), "Cleanup"))
        assertEquals(listOf(a, b), SnapshotListLogic.filter(listOf(a, b), "  "))
    }
}

class EmptyStateTest {
    @Test fun emptyListAndNoResultsHaveDifferentMessages() {
        val empty = SnapshotListLogic.emptyMessage(0, 0, "")
        val noHits = SnapshotListLogic.emptyMessage(3, 0, "zzz")
        assertNotNull(empty); assertNotNull(noHits)
        assertTrue(empty != noHits)
        assertNull(SnapshotListLogic.emptyMessage(3, 2, "a"))
    }
}
