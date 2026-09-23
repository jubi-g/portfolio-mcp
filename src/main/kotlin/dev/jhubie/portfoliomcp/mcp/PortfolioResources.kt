package dev.jhubie.portfoliomcp.mcp

import dev.jhubie.portfoliomcp.service.PortfolioService
import org.springframework.ai.mcp.annotation.McpResource
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/** Read-only MCP resources: whole-section JSON views under `portfolio://` URIs. */
@Component
class PortfolioResources(
    private val service: PortfolioService,
    private val objectMapper: ObjectMapper,
) {

    @McpResource(
        uri = "portfolio://profile",
        name = "profile",
        description = "The professional profile.",
        mimeType = "application/json",
    )
    fun profile(): String = toJson(service.getProfile())

    @McpResource(
        uri = "portfolio://experience",
        name = "experience",
        description = "All work experience, newest first.",
        mimeType = "application/json",
    )
    fun experience(): String = toJson(service.getExperience(null, null))

    @McpResource(
        uri = "portfolio://skills",
        name = "skills",
        description = "All skills.",
        mimeType = "application/json",
    )
    fun skills(): String = toJson(service.getSkills(null))

    @McpResource(
        uri = "portfolio://projects",
        name = "projects",
        description = "All projects.",
        mimeType = "application/json",
    )
    fun projects(): String = toJson(service.getProjects())

    @McpResource(
        uri = "portfolio://education",
        name = "education",
        description = "All education entries.",
        mimeType = "application/json",
    )
    fun education(): String = toJson(service.getEducation())

    @McpResource(
        uri = "portfolio://timeline",
        name = "timeline",
        description = "Chronological career timeline (newest first), merging experience and education.",
        mimeType = "application/json",
    )
    fun timeline(): String = toJson(service.getCareerTimeline(true))

    private fun toJson(value: Any): String = objectMapper.writeValueAsString(value)
}
