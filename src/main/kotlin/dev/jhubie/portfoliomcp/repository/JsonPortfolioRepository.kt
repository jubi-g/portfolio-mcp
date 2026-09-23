package dev.jhubie.portfoliomcp.repository

import dev.jhubie.portfoliomcp.domain.Portfolio
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Repository
import tools.jackson.databind.ObjectMapper

/**
 * Loads the portfolio once at construction, failing fast on missing/malformed data.
 * Source resolution order lives in [resolveAndLoad]: env JSON, configured location,
 * own gitignored file, then the shipped sample.
 */
@Repository
class JsonPortfolioRepository(
    @Value("\${PORTFOLIO_DATA_JSON:}") inlineJson: String,
    @Value("\${portfolio.data.location:}") configuredLocation: String,
    resourceLoader: ResourceLoader,
    objectMapper: ObjectMapper,
) : PortfolioRepository {

    private val portfolio: Portfolio =
        resolveAndLoad(inlineJson, configuredLocation, resourceLoader, objectMapper)
            .also { log.info("Loaded portfolio for '{}'", it.profile.name) }

    override fun getPortfolio(): Portfolio = portfolio

    private fun resolveAndLoad(
        inlineJson: String,
        configuredLocation: String,
        resourceLoader: ResourceLoader,
        objectMapper: ObjectMapper,
    ): Portfolio {
        if (inlineJson.isNotBlank()) {
            log.info("Loading portfolio from PORTFOLIO_DATA_JSON environment variable")
            return objectMapper.readValue(inlineJson, Portfolio::class.java)
        }
        if (configuredLocation.isNotBlank()) {
            return loadResource(resourceLoader.getResource(configuredLocation), objectMapper)
        }
        val ownData = resourceLoader.getResource(OWN_DATA)
        if (ownData.exists()) {
            return loadResource(ownData, objectMapper)
        }
        log.info("No portfolio.json found; falling back to the shipped sample")
        return loadResource(resourceLoader.getResource(SAMPLE_DATA), objectMapper)
    }

    private fun loadResource(resource: Resource, objectMapper: ObjectMapper): Portfolio {
        log.info("Loading portfolio from {}", resource.description)
        return resource.inputStream.use { objectMapper.readValue(it, Portfolio::class.java) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(JsonPortfolioRepository::class.java)
        private const val OWN_DATA = "classpath:portfolio.json"
        private const val SAMPLE_DATA = "classpath:portfolio.sample.json"
    }
}
