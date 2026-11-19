package com.se7team.caughtu.domain

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

/** Raw form values as the user typed them. Pure Kotlin so it can be unit-tested on the JVM. */
data class SnapshotForm(
    val label: String = "",
    val note: String = "",
    val typeId: String = "",
    val exportedAt: Long? = null,          // epoch millis, null = not set
    val followerCount: String = "",
    val followingCount: String = "",
    val notFollowingBackCount: String = "",
)

/** One message per field, null = valid. */
data class SnapshotErrors(
    val label: String? = null,
    val note: String? = null,
    val typeId: String? = null,
    val exportedAt: String? = null,
    val followerCount: String? = null,
    val followingCount: String? = null,
    val notFollowingBackCount: String? = null,
) {
    val isValid: Boolean
        get() = listOf(label, note, typeId, exportedAt, followerCount, followingCount, notFollowingBackCount)
            .all { it == null }
}

object SnapshotValidator {
    const val MAX_LABEL = 40
    const val MAX_NOTE = 200
    const val MAX_COUNT = 10_000_000L

    fun validateLabel(label: String): String? = when {
        label.isBlank() -> "Label is required"
        label.trim().length > MAX_LABEL -> "Maximum $MAX_LABEL characters"
        else -> null
    }

    fun validateNote(note: String): String? =
        if (note.trim().length > MAX_NOTE) "Maximum $MAX_NOTE characters" else null

    fun validateType(typeId: String): String? =
        if (typeId.isBlank()) "Pick a snapshot type" else null

    fun validateExportedAt(exportedAt: Long?, now: Long): String? = when {
        exportedAt == null -> "Export date is required"
        exportedAt > now -> "Export date can't be in the future"
        else -> null
    }

    /** Parses YYYY-MM-DD to UTC-midnight epoch millis, or null if the text is not a real date. */
    fun parseDate(text: String): Long? = try {
        LocalDate.parse(text.trim()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }

    /** Returns the parsed number, or null if the text is not a whole number in range. */
    fun parseCount(text: String): Long? =
        text.trim().toLongOrNull()?.takeIf { it in 0..MAX_COUNT }

    fun validateCount(text: String): String? = when {
        text.isBlank() -> "Required"
        parseCount(text) == null -> "Enter a whole number from 0 to $MAX_COUNT"
        else -> null
    }

    /** Cross-field rule: you can't have more people not following back than people you follow. */
    fun validateNotFollowingBack(notFollowingBack: String, following: String): String? {
        validateCount(notFollowingBack)?.let { return it }
        val nfb = parseCount(notFollowingBack)!!
        val fol = parseCount(following) ?: return null // the following field shows its own error
        return if (nfb > fol) "Can't be more than Following ($fol)" else null
    }

    fun validate(form: SnapshotForm, now: Long): SnapshotErrors = SnapshotErrors(
        label = validateLabel(form.label),
        note = validateNote(form.note),
        typeId = validateType(form.typeId),
        exportedAt = validateExportedAt(form.exportedAt, now),
        followerCount = validateCount(form.followerCount),
        followingCount = validateCount(form.followingCount),
        notFollowingBackCount = validateNotFollowingBack(form.notFollowingBackCount, form.followingCount),
    )
}
