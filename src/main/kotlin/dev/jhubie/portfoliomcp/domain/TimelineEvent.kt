package dev.jhubie.portfoliomcp.domain

import java.time.LocalDate

/** Derived by merging experience and education; a `null` endDate means ongoing. */
data class TimelineEvent(
    val date: LocalDate,
    val type: EventType,
    val title: String,
    val organization: String,
    val endDate: LocalDate?,
)
