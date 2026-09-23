# Development

Contributor guide: how the project is put together, how to build and test it, and how to extend it.
For using the server, see the [README](../README.md).

## Requirements

- Java 21
- Maven
- Docker (optional, for the container build)

## Project layout

```
src/main/kotlin/dev/jhubie/portfoliomcp/
├── PortfolioMcpApplication.kt       # Spring Boot entry point
├── domain/                          # data classes: Profile, Experience, Skill, Project, Education, …
├── repository/                      # PortfolioRepository (interface) + JsonPortfolioRepository
├── service/                         # PortfolioService: get / filter / search / timeline
└── mcp/                             # PortfolioTools, PortfolioResources, PortfolioPrompts
src/main/resources/
├── application.yml                  # config (port, MCP protocol, actuator)
└── portfolio.json                   # the portfolio data
```

### Architecture

```
MCP client
   │  Streamable HTTP (stateless), POST /mcp
   ▼
mcp/         @McpTool / @McpResource / @McpPrompt  (thin, no logic)
   ▼
service/     PortfolioService
   ▼
repository/  PortfolioRepository → portfolio.json
   ▼
domain/      data classes
```

The dependency arrow only ever points down: `mcp → service → repository → domain`. The domain,
service, and repository never import anything MCP-related. That is what lets you swap the data source
or the transport without touching the rest.

## Build & test

```bash
mvn test             # run the test suite
mvn package          # build the executable jar into target/
mvn spring-boot:run
```

Test layers: domain/service logic, JSON parsing and fail-fast, tool validation and responses,
resources, prompts, and an end-to-end integration test that drives the real `/mcp` endpoint on a
random port.

## Configuration

Set via `application.yml` or environment variables:

| Setting | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | HTTP port (the app binds `0.0.0.0:${PORT}`) |
| `portfolio.data.location` | `classpath:portfolio.json` | Where portfolio data is loaded from |
| `spring.ai.mcp.server.protocol` | `STATELESS` | MCP transport mode |

## Swap the data source

The whole point of the repository seam is that the data need not be a bundled JSON file. Implement the
interface and drop your bean in — nothing else changes:

```kotlin
@Repository
class DbPortfolioRepository : PortfolioRepository {
    override fun getPortfolio(): Portfolio {
        // load from a database, an API, etc.
    }
}
```

The service and MCP layers depend only on `PortfolioRepository`, so they are unaffected.

## Container build

```bash
docker build -t portfolio-mcp .
docker run -p 8080:8080 portfolio-mcp
```

Multi-stage build (Maven → JRE), runs as a non-root user, binds `0.0.0.0:${PORT:-8080}`. The same
image runs on any container platform — set `PORT` if the platform injects one.

## Hosting

The image needs no secrets and no database, so any container host works (Railway, Fly, Cloud Run, a
plain VM): point the platform at the root `Dockerfile`, expose a public URL, and use
`/actuator/health` as the health check. TLS is expected to terminate at the platform edge; the app
serves plain HTTP on `$PORT`.
