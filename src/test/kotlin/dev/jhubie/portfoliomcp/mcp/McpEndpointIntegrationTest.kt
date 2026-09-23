package dev.jhubie.portfoliomcp.mcp

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

/** End-to-end: drives the real POST /mcp JSON-RPC endpoint as an MCP client would. */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = ["portfolio.data.location=classpath:portfolio.sample.json"])
class McpEndpointIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private fun client(): RestClient =
        RestClient.builder().baseUrl("http://localhost:$port").build()

    private fun rpc(method: String, paramsJson: String?): JsonNode {
        val body = buildString {
            append("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"$method\"")
            if (paramsJson != null) append(",\"params\":$paramsJson")
            append("}")
        }
        val response = client().post()
            .uri("/mcp")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
            .body(body)
            .retrieve()
            .body(String::class.java)
        return objectMapper.readTree(response).path("result")
    }

    @Test
    fun testToolsListExposesAllSixTools() {
        val tools = rpc("tools/list", null).path("tools")

        assertThat(tools).hasSize(6)
    }

    @Test
    fun testResourcesListExposesAllSixResources() {
        val resources = rpc("resources/list", null).path("resources")

        assertThat(resources).hasSize(6)
    }

    @Test
    fun testPromptsListExposesBothPrompts() {
        val prompts = rpc("prompts/list", null).path("prompts")

        assertThat(prompts).hasSize(2)
    }

    @Test
    fun testCallGetProfileReturnsName() {
        val result = rpc("tools/call", "{\"name\":\"get_profile\",\"arguments\":{}}")

        val text = result.path("content").get(0).path("text").asString()
        assertThat(text).contains("Ada Example")
    }

    @Test
    fun testCallGetProjectUnknownReturnsError() {
        val result = rpc(
            "tools/call",
            "{\"name\":\"get_project\",\"arguments\":{\"name\":\"does-not-exist\"}}",
        )

        assertThat(result.path("isError").asBoolean()).isTrue()
    }

    @Test
    fun testReadProfileResourceReturnsJson() {
        val result = rpc("resources/read", "{\"uri\":\"portfolio://profile\"}")

        val text = result.path("contents").get(0).path("text").asString()
        assertThat(text).contains("Ada Example")
    }

    @Test
    fun testGetProfessionalSummaryPromptInjectsPortfolio() {
        val result = rpc(
            "prompts/get",
            "{\"name\":\"professional_summary\",\"arguments\":{\"audience\":\"recruiter\"}}",
        )

        val text = result.path("messages").get(0).path("content").path("text").asString()
        assertThat(text).contains("recruiter")
    }
}
