package dev.jhubie.portfoliomcp.domain

/** `type` names the source section (e.g. "project", "experience"). */
data class SearchResult(
    val type: String,
    val title: String,
    val snippet: String,
    val score: Int,
)
