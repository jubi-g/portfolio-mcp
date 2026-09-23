package dev.jhubie.portfoliomcp.mcp

import dev.jhubie.portfoliomcp.domain.Experience
import dev.jhubie.portfoliomcp.service.PortfolioService
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult
import io.modelcontextprotocol.spec.McpSchema.PromptMessage
import io.modelcontextprotocol.spec.McpSchema.Role
import io.modelcontextprotocol.spec.McpSchema.TextContent
import org.springframework.ai.mcp.annotation.McpArg
import org.springframework.ai.mcp.annotation.McpPrompt
import org.springframework.stereotype.Component

/** User-invoked MCP prompts that inject portfolio context into a ready-to-run instruction. */
@Component
class PortfolioPrompts(private val service: PortfolioService) {

    @McpPrompt(
        name = "professional_summary",
        description = "Draft a tailored professional summary grounded in the portfolio.",
    )
    fun professionalSummary(
        @McpArg(
            name = "audience",
            description = "Who the summary is for (e.g. recruiter, engineering manager)",
            required = false,
        )
        audience: String?,
        @McpArg(
            name = "tone",
            description = "Desired tone (e.g. concise, warm, formal)",
            required = false,
        )
        tone: String?,
    ): GetPromptResult {
        val text = """
            Write a professional summary for ${service.getProfile().name}.
            Audience: ${audience ?: "a general professional audience"}.
            Tone: ${tone ?: "concise and professional"}.

            Base it only on the portfolio below. Do not invent facts.

            ${portfolioContext()}
            """.trimIndent()
        return userTextResult("Draft a professional summary", text)
    }

    @McpPrompt(
        name = "interview_preparation",
        description = "Generate interview questions and talking points grounded in the portfolio.",
    )
    fun interviewPreparation(
        @McpArg(name = "role", description = "The role being interviewed for", required = false)
        role: String?,
        @McpArg(name = "company", description = "The company (optional)", required = false)
        company: String?,
        @McpArg(
            name = "focus",
            description = "Focus area (e.g. system design, leadership)",
            required = false,
        )
        focus: String?,
    ): GetPromptResult {
        val roleClause = if (role.isNullOrBlank()) "" else " for the role of $role"
        val companyClause = if (company.isNullOrBlank()) "" else " at $company"
        val text = """
            Help ${service.getProfile().name} prepare for an interview$roleClause$companyClause.
            Focus: ${focus ?: "general software engineering"}.

            Using only the portfolio below, generate likely interview questions and
            concrete talking points drawn from the person's real experience, projects
            and skills. Do not invent facts.

            ${portfolioContext()}
            """.trimIndent()
        return userTextResult("Prepare for an interview", text)
    }

    private fun portfolioContext(): String {
        val profile = service.getProfile()
        val experience = service.getExperience(null, null).joinToString("\n", transform = ::formatExperience)
        val skills = service.getSkills(null).joinToString(", ") { it.name }
        return """
            PROFILE:
            ${profile.name} — ${profile.headline}
            ${profile.summary}

            EXPERIENCE:
            $experience

            SKILLS:
            $skills
            """.trimIndent()
    }

    private fun formatExperience(e: Experience): String {
        val period = "${e.startDate.year}–${e.endDate?.year ?: "present"}"
        return "- ${e.role} at ${e.company} ($period): ${e.description}"
    }

    private fun userTextResult(description: String, text: String): GetPromptResult {
        val message = PromptMessage(Role.USER, TextContent(text))
        return GetPromptResult(description, listOf(message))
    }
}
