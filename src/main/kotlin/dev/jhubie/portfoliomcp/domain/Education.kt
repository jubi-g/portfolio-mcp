package dev.jhubie.portfoliomcp.domain

import java.time.LocalDate

/** A `null` endDate means ongoing. */
data class Education(
    val institution: String,
    val degree: String,
    val field: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)
