package dev.jhubie.portfoliomcp.mcp

import dev.jhubie.portfoliomcp.domain.Experience
import dev.jhubie.portfoliomcp.domain.Profile
import dev.jhubie.portfoliomcp.domain.Project
import dev.jhubie.portfoliomcp.domain.SearchResult
import dev.jhubie.portfoliomcp.domain.Skill
import dev.jhubie.portfoliomcp.domain.TimelineEvent
import dev.jhubie.portfoliomcp.service.PortfolioService
import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpTool.McpAnnotations
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component

/** Model-invoked MCP tools; each delegates to [PortfolioService]. */
@Component
class PortfolioTools(private val service: PortfolioService) {

    @McpTool(
        name = "get_profile",
        description = "Get the professional profile: name, headline, summary, location and public links.",
        annotations = McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getProfile(): Profile = service.getProfile()

    @McpTool(
        name = "get_experience",
        description = "Get work experience, newest first, optionally filtered by company and/or technology.",
        annotations = McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getExperience(
        @McpToolParam(
            description = "Filter by company name (case-insensitive, partial match)",
            required = false,
        )
        company: String?,
        @McpToolParam(
            description = "Filter by a technology used in the role (case-insensitive)",
            required = false,
        )
        technology: String?,
    ): List<Experience> = service.getExperience(company, technology)

    @McpTool(
        name = "get_skills",
        description = "Get skills, optionally filtered by category (e.g. Languages, Frameworks, Data).",
        annotations = McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getSkills(
        @McpToolParam(
            description = "Filter by skill category (case-insensitive, exact match)",
            required = false,
        )
        category: String?,
    ): List<Skill> = service.getSkills(category)

    @McpTool(
        name = "get_project",
        description = "Get a single portfolio project by its exact name.",
        annotations = McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getProject(
        @McpToolParam(description = "The exact project name", required = true)
        name: String,
    ): Project = service.getProject(name)
        ?: throw IllegalArgumentException("No project named '$name'")

    @McpTool(
        name = "search_portfolio",
        description = "Free-text search across profile, experience, skills, projects and education. " +
            "Returns ranked results, most relevant first.",
        annotations = McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun searchPortfolio(
        @McpToolParam(description = "The search query", required = true)
        query: String,
        @McpToolParam(
            description = "Maximum number of results (default 20, max 100)",
            required = false,
        )
        limit: Int?,
    ): List<SearchResult> {
        require(query.isNotBlank()) { "query must not be blank" }
        val effectiveLimit = limit?.coerceIn(1, MAX_SEARCH_LIMIT) ?: DEFAULT_SEARCH_LIMIT
        return service.search(query, effectiveLimit)
    }

    @McpTool(
        name = "get_career_timeline",
        description = "Get a chronological timeline merging work experience and education. " +
            "Defaults to newest first.",
        annotations = McpAnnotations(
            readOnlyHint = true,
            destructiveHint = false,
            idempotentHint = true,
            openWorldHint = false,
        ),
    )
    fun getCareerTimeline(
        @McpToolParam(
            description = "Order: DESC (newest first, default) or ASC (oldest first)",
            required = false,
        )
        order: String?,
    ): List<TimelineEvent> {
        val descending = order == null || !order.equals("ASC", ignoreCase = true)
        return service.getCareerTimeline(descending)
    }

    companion object {
        private const val DEFAULT_SEARCH_LIMIT = 20
        private const val MAX_SEARCH_LIMIT = 100
    }
}
