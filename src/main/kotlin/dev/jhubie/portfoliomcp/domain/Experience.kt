package dev.jhubie.portfoliomcp.domain

import java.time.LocalDate

/** A `null` endDate means the role is current. */
data class Experience(
    val company: String,
    val role: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val description: String,
    val technologies: List<String>,
    val highlights: List<String>,
)
