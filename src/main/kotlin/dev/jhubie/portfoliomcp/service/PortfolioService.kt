package dev.jhubie.portfoliomcp.service

import dev.jhubie.portfoliomcp.domain.Education
import dev.jhubie.portfoliomcp.domain.Experience
import dev.jhubie.portfoliomcp.domain.EventType.EDUCATION
import dev.jhubie.portfoliomcp.domain.EventType.EXPERIENCE
import dev.jhubie.portfoliomcp.domain.Profile
import dev.jhubie.portfoliomcp.domain.Project
import dev.jhubie.portfoliomcp.domain.SearchResult
import dev.jhubie.portfoliomcp.domain.Skill
import dev.jhubie.portfoliomcp.domain.TimelineEvent
import dev.jhubie.portfoliomcp.repository.PortfolioRepository
import org.springframework.stereotype.Service

/** Read/filter/search/timeline logic over the portfolio; no MCP or web concerns. */
@Service
class PortfolioService(private val repository: PortfolioRepository) {

    fun getProfile(): Profile = portfolio().profile

    fun getExperience(company: String?, technology: String?): List<Experience> =
        portfolio().experience
            .filter { matches(it.company, company) }
            .filter { technology.isNullOrBlank() || containsIgnoreCase(it.technologies, technology) }
            .sortedByDescending { it.startDate }

    fun getSkills(category: String?): List<Skill> =
        portfolio().skills.filter { category.isNullOrBlank() || it.category.equals(category, ignoreCase = true) }

    fun getProjects(): List<Project> = portfolio().projects

    fun getEducation(): List<Education> = portfolio().education

    fun getProject(name: String?): Project? {
        if (name.isNullOrBlank()) return null
        return portfolio().projects.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
    }

    fun getCareerTimeline(descending: Boolean): List<TimelineEvent> {
        val events = buildList {
            portfolio().experience.forEach {
                add(TimelineEvent(it.startDate, EXPERIENCE, it.role, it.company, it.endDate))
            }
            portfolio().education.forEach {
                add(TimelineEvent(it.startDate, EDUCATION, "${it.degree} in ${it.field}", it.institution, it.endDate))
            }
        }
        return if (descending) events.sortedByDescending { it.date } else events.sortedBy { it.date }
    }

    fun search(query: String, limit: Int): List<SearchResult> {
        val q = query.trim().lowercase()
        val results = buildList {
            val profile = portfolio().profile
            score(q, profile.name, profile.headline, profile.summary).takeIf { it > 0 }?.let {
                add(SearchResult("profile", profile.name, profile.headline, it))
            }
            portfolio().experience.forEach { e ->
                val s = score(q, e.company, e.role, e.description) +
                    scoreList(q, e.technologies) + scoreList(q, e.highlights)
                if (s > 0) add(SearchResult("experience", "${e.role} @ ${e.company}", e.description, s))
            }
            portfolio().projects.forEach { p ->
                val s = score(q, p.name, p.description) +
                    scoreList(q, p.technologies) + scoreList(q, p.highlights)
                if (s > 0) add(SearchResult("project", p.name, p.description, s))
            }
            portfolio().skills.forEach { sk ->
                val s = score(q, sk.name, sk.category)
                if (s > 0) add(SearchResult("skill", sk.name, sk.category, s))
            }
            portfolio().education.forEach { ed ->
                val s = score(q, ed.institution, ed.degree, ed.field)
                if (s > 0) add(SearchResult("education", "${ed.degree} in ${ed.field}", ed.institution, s))
            }
        }
        return results.sortedByDescending { it.score }.take(limit)
    }

    private fun portfolio() = repository.getPortfolio()

    private fun matches(value: String?, filter: String?): Boolean =
        filter.isNullOrBlank() || (value ?: "").lowercase().contains(filter.lowercase())

    private fun containsIgnoreCase(values: List<String>, needle: String): Boolean =
        values.any { it.equals(needle, ignoreCase = true) }

    private fun score(query: String, vararg fields: String?): Int =
        fields.count { it != null && it.lowercase().contains(query) }

    private fun scoreList(query: String, values: List<String>): Int =
        values.count { it.lowercase().contains(query) }
}
