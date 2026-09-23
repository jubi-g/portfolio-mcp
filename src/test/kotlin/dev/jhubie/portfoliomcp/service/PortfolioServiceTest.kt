package dev.jhubie.portfoliomcp.service

import dev.jhubie.portfoliomcp.domain.EventType
import dev.jhubie.portfoliomcp.domain.TimelineEvent
import dev.jhubie.portfoliomcp.repository.PortfolioRepository
import dev.jhubie.portfoliomcp.service.PortfolioTestData.profile
import dev.jhubie.portfoliomcp.service.PortfolioTestData.validPortfolio
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

class PortfolioServiceTest {

    private lateinit var service: PortfolioService

    @BeforeEach
    fun setUp() {
        val repository = mock<PortfolioRepository>()
        whenever(repository.getPortfolio()).thenReturn(validPortfolio())
        service = PortfolioService(repository)
    }

    @Test
    fun testGetProfileReturnsProfile() {
        assertThat(service.getProfile()).isEqualTo(profile())
    }

    @Nested
    inner class Experiences {

        @Test
        fun testGetExperienceReturnsNewestFirst() {
            val companies = service.getExperience(null, null).map { it.company }

            assertThat(companies).containsExactly("US Navy", "Eckert-Mauchly")
        }

        @Test
        fun testGetExperienceFiltersByCompany() {
            val result = service.getExperience("navy", null).map { it.company }

            assertThat(result).containsExactly("US Navy")
        }

        @Test
        fun testGetExperienceFiltersByTechnology() {
            val result = service.getExperience(null, "A-0").map { it.company }

            assertThat(result).containsExactly("Eckert-Mauchly")
        }

        @Test
        fun testGetExperienceReturnsEmptyWhenNoMatch() {
            assertThat(service.getExperience(null, "Rust")).isEmpty()
        }
    }

    @Nested
    inner class Skills {

        @Test
        fun testGetSkillsFiltersByCategoryCaseInsensitively() {
            val names = service.getSkills("languages").map { it.name }

            assertThat(names).containsExactly("COBOL")
        }

        @Test
        fun testGetSkillsReturnsAllWhenCategoryBlank() {
            assertThat(service.getSkills(" ")).hasSize(3)
        }
    }

    @Nested
    inner class Projects {

        @Test
        fun testGetProjectFindsByNameCaseInsensitively() {
            assertThat(service.getProject("cobol")?.name).isEqualTo("COBOL")
        }

        @Test
        fun testGetProjectReturnsNullForUnknownName() {
            assertThat(service.getProject("nonexistent")).isNull()
        }

        @Test
        fun testGetProjectReturnsNullForBlankName() {
            assertThat(service.getProject("  ")).isNull()
        }
    }

    @Nested
    inner class Timeline {

        @Test
        fun testTimelineDescendingStartsWithMostRecent() {
            val first = service.getCareerTimeline(true).first()

            val expected = TimelineEvent(
                LocalDate.of(1967, 1, 1), EventType.EXPERIENCE,
                "Rear Admiral", "US Navy", null,
            )
            assertThat(first).isEqualTo(expected)
        }

        @Test
        fun testTimelineAscendingStartsWithEarliest() {
            val first = service.getCareerTimeline(false).first()

            val expected = TimelineEvent(
                LocalDate.of(1924, 9, 1), EventType.EDUCATION,
                "BA in Mathematics and Physics", "Vassar College",
                LocalDate.of(1928, 6, 1),
            )
            assertThat(first).isEqualTo(expected)
        }

        @Test
        fun testTimelineMergesExperienceAndEducation() {
            assertThat(service.getCareerTimeline(true)).hasSize(4)
        }
    }

    @Nested
    inner class Search {

        @Test
        fun testSearchFindsProjectByName() {
            val titles = service.search("cobol", 20).map { it.title }

            assertThat(titles).contains("COBOL")
        }

        @Test
        fun testSearchIsCaseInsensitive() {
            assertThat(service.search("COMPILER", 20)).isNotEmpty()
        }

        @Test
        fun testSearchReturnsEmptyForNoMatch() {
            assertThat(service.search("kubernetes", 20)).isEmpty()
        }

        @Test
        fun testSearchRanksHigherScoreFirst() {
            val results = service.search("compiler", 20)

            assertThat(results).isSortedAccordingTo { a, b -> b.score.compareTo(a.score) }
        }

        @Test
        fun testSearchRespectsLimit() {
            assertThat(service.search("a", 2)).hasSizeLessThanOrEqualTo(2)
        }
    }
}
