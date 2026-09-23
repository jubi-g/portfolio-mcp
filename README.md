# portfolio-mcp

A reusable [Model Context Protocol](https://modelcontextprotocol.io) (MCP) server for professional
portfolios. Point an MCP-compatible AI assistant at it and it can read a portfolio through a **fixed,
deterministic** interface — the same six tools, six resources, and two prompts regardless of whose
data is loaded.

Built with Java 21, Spring Boot, and Spring AI. Stateless, no database, runs anywhere as a container.

> **You don't need to deploy anything to use this.** Clone it, drop in your data, run it locally, and
> connect your AI client. Hosting it is optional (see [docs/development.md](docs/development.md)).

## The interface

The interface is hand-written and fixed — it is **not** generated from your data.

**Tools** (the model calls these on demand):

| Tool | Purpose | Arguments |
|---|---|---|
| `get_profile` | The professional profile | — |
| `get_experience` | Work experience, newest first | `company?`, `technology?` |
| `get_skills` | Skills, optionally by category | `category?` |
| `get_project` | A single project by exact name | `name` (required) |
| `search_portfolio` | Ranked free-text search across everything | `query` (required), `limit?` |
| `get_career_timeline` | Chronological merge of experience + education | `order?` (`ASC`/`DESC`) |

**Resources** (whole sections, read by URI): `portfolio://profile`, `portfolio://experience`,
`portfolio://skills`, `portfolio://projects`, `portfolio://education`, `portfolio://timeline`.

**Prompts** (you invoke these): `professional_summary` (`audience?`, `tone?`) and
`interview_preparation` (`role?`, `company?`, `focus?`).

## Use it

### 1. Add your data

Edit [`src/main/resources/portfolio.json`](src/main/resources/portfolio.json) — it ships with an
example you can replace. That one file is the only thing you need to change.

### 2. Run it

**With Docker** (nothing else to install):

```bash
docker build -t portfolio-mcp .
docker run -p 8080:8080 portfolio-mcp
```

**Or with Java 21 + Maven:**

```bash
./mvnw spring-boot:run
```

Either way the server is now at `http://localhost:8080/mcp`, with a health check at
`http://localhost:8080/actuator/health`.

### 3. Connect an AI client

The transport is **Streamable HTTP** at `POST /mcp`. The quickest way to explore it is the
[MCP Inspector](https://github.com/modelcontextprotocol/inspector):

```bash
npx @modelcontextprotocol/inspector
```

In the UI: **Transport type** → `Streamable HTTP`, **URL** → `http://localhost:8080/mcp` → **Connect**.
You can now browse and call every tool, resource, and prompt.

To wire it into an MCP client that reads a config file, point it at the same URL with the Streamable
HTTP transport.

### Quick check without a client

```bash
curl localhost:8080/actuator/health

curl -s -X POST localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'
```

The `Accept` header must include both `application/json` and `text/event-stream` — the stateless
transport requires it.

## Make it your own

- **Change the data:** replace `src/main/resources/portfolio.json`. Nothing else changes.
- **Point at a different file:** set `portfolio.data.location` (any Spring resource path, e.g.
  `file:/data/portfolio.json`) to load data without rebuilding.

For project layout, building, testing, hosting, and how to swap the data source for a database or
API, see **[docs/development.md](docs/development.md)**.

## Security

This server exposes **public, read-only** portfolio data over an **unauthenticated** endpoint by
design. Only include information you are happy to publish — there are no secrets, no mutations, and no
database. If you later expose private data, put authentication in front of the endpoint.

## License

MIT
