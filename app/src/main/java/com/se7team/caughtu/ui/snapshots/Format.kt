package com.se7team.caughtu.ui.snapshots

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Export dates are stored as UTC midnight, so they are shown in UTC to avoid an off-by-one day.
private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH).apply { timeZone = TimeZone.getTimeZone("UTC") }

fun formatDate(millis: Long): String = dateFormat.format(Date(millis))
