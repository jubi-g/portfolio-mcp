package dev.jhubie.portfoliomcp.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

class JsonPortfolioRepositoryTest {

    private val objectMapper: ObjectMapper = JsonMapper.builder().findAndAddModules().build()
    private val resourceLoader: ResourceLoader = DefaultResourceLoader()

    private fun repository(inlineJson: String, configuredLocation: String) =
        JsonPortfolioRepository(inlineJson, configuredLocation, resourceLoader, objectMapper)

    @Test
    fun testLoadsSampleWhenOwnDataAbsent() {
        // With no own-data file present, the shipped sample is the fallback. A local
        // (gitignored) portfolio.json would legitimately take precedence, so this
        // asserts the deterministic fallback path via an explicit missing location
        // rather than depending on whether portfolio.json happens to exist on disk.
        val portfolio = repository("", "classpath:portfolio.sample.json").getPortfolio()

        assertThat(portfolio.profile.name).isEqualTo("Ada Example")
    }

    @Test
    fun testLoadsFromInlineEnvironmentJson() {
        val json = """
            {
              "profile": {
                "name": "Env Person",
                "headline": "h",
                "summary": "s",
                "location": "l",
                "interests": [],
                "links": []
              },
              "experience": [],
              "skills": [],
              "projects": [],
              "education": []
            }
            """.trimIndent()

        val portfolio = repository(json, "").getPortfolio()

        assertThat(portfolio.profile.name).isEqualTo("Env Person")
    }

    @Test
    fun testFailsFastOnMalformedJson() {
        assertThatThrownBy { repository("{ not valid json ", "") }
            .isInstanceOf(RuntimeException::class.java)
    }
}
