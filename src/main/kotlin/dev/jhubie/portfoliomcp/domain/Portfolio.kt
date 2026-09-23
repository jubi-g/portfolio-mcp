package dev.jhubie.portfoliomcp.domain

data class Portfolio(
    val profile: Profile,
    val experience: List<Experience>,
    val skills: List<Skill>,
    val projects: List<Project>,
    val education: List<Education>,
)
