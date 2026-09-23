package dev.jhubie.portfoliomcp.mcp

import dev.jhubie.portfoliomcp.domain.Project
import dev.jhubie.portfoliomcp.service.PortfolioService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PortfolioToolsTest {

    private lateinit var service: PortfolioService
    private lateinit var tools: PortfolioTools

    @BeforeEach
    fun setUp() {
        service = mock()
        tools = PortfolioTools(service)
    }

    @Test
    fun testGetProjectReturnsProjectWhenFound() {
        val project = Project("portfolio-mcp", "desc", emptyList(), emptyList(), emptyList())
        whenever(service.getProject("portfolio-mcp")).thenReturn(project)

        assertThat(tools.getProject("portfolio-mcp")).isEqualTo(project)
    }

    @Test
    fun testGetProjectThrowsWhenUnknown() {
        whenever(service.getProject("nope")).thenReturn(null)

        assertThatThrownBy { tools.getProject("nope") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("nope")
    }

    @Test
    fun testSearchPortfolioRejectsBlankQuery() {
        assertThatThrownBy { tools.searchPortfolio("  ", null) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun testSearchPortfolioClampsLimitToMaximum() {
        tools.searchPortfolio("java", 5000)

        verify(service).search("java", 100)
    }

    @Test
    fun testSearchPortfolioDefaultsLimitWhenAbsent() {
        tools.searchPortfolio("java", null)

        verify(service).search("java", 20)
    }

    @Test
    fun testGetCareerTimelineDefaultsToDescending() {
        whenever(service.getCareerTimeline(true)).thenReturn(emptyList())

        assertThat(tools.getCareerTimeline(null)).isEmpty()
    }

    @Test
    fun testGetCareerTimelineAscendingWhenRequested() {
        whenever(service.getCareerTimeline(false)).thenReturn(emptyList())

        assertThat(tools.getCareerTimeline("ASC")).isEmpty()
    }
}
