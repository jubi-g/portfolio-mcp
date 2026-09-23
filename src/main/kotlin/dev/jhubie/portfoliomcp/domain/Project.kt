package dev.jhubie.portfoliomcp.domain

data class Project(
    val name: String,
    val description: String,
    val technologies: List<String>,
    val highlights: List<String>,
    val links: List<Link>,
)
