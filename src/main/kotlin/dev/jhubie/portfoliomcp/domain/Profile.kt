package dev.jhubie.portfoliomcp.domain

data class Profile(
    val name: String,
    val headline: String,
    val summary: String,
    val location: String,
    val interests: List<String>,
    val links: List<Link>,
)
