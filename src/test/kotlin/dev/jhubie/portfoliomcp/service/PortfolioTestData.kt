package dev.jhubie.portfoliomcp.service

import dev.jhubie.portfoliomcp.domain.Education
import dev.jhubie.portfoliomcp.domain.Experience
import dev.jhubie.portfoliomcp.domain.Link
import dev.jhubie.portfoliomcp.domain.Portfolio
import dev.jhubie.portfoliomcp.domain.Profile
import dev.jhubie.portfoliomcp.domain.Project
import dev.jhubie.portfoliomcp.domain.Skill
import java.time.LocalDate

/** Immutable fixture: one known portfolio for whole-object assertions. */
object PortfolioTestData {

    fun validPortfolio() = Portfolio(profile(), experience(), skills(), projects(), education())

    fun profile() = Profile(
        "Grace Hopper",
        "Compiler pioneer",
        "Built the first compiler and championed machine-independent languages.",
        "New York, USA",
        listOf("Compilers", "Standardisation"),
        listOf(Link("GitHub", "https://github.com/grace")),
    )

    fun experience() = listOf(
        Experience(
            "US Navy", "Rear Admiral",
            LocalDate.of(1967, 1, 1), null,
            "Standardised COBOL and testing.",
            listOf("COBOL", "Standards"),
            listOf("Standardised COBOL across the Navy"),
        ),
        Experience(
            "Eckert-Mauchly", "Senior Mathematician",
            LocalDate.of(1949, 1, 1), LocalDate.of(1959, 12, 31),
            "Worked on UNIVAC I and the A-0 compiler.",
            listOf("UNIVAC", "A-0"),
            listOf("Wrote the first compiler (A-0)"),
        ),
    )

    fun skills() = listOf(
        Skill("COBOL", "Languages"),
        Skill("Compilers", "Systems"),
        Skill("Public Speaking", "Leadership"),
    )

    fun projects() = listOf(
        Project(
            "A-0 Compiler",
            "The first compiler, translating symbolic code to machine code.",
            listOf("UNIVAC", "Assembly"),
            listOf("First working compiler"),
            listOf(Link("History", "https://example.com/a0")),
        ),
        Project(
            "COBOL",
            "A machine-independent business language.",
            listOf("COBOL"),
            listOf("Widely adopted in business computing"),
            emptyList(),
        ),
    )

    fun education() = listOf(
        Education(
            "Yale University", "PhD", "Mathematics",
            LocalDate.of(1930, 9, 1), LocalDate.of(1934, 6, 1),
        ),
        Education(
            "Vassar College", "BA", "Mathematics and Physics",
            LocalDate.of(1924, 9, 1), LocalDate.of(1928, 6, 1),
        ),
    )
}
