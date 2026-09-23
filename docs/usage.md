# Usage

How to run portfolio-mcp yourself and connect an AI client. For a one-click hosted deploy, use the
Railway button in the [README](../README.md). For project internals and contributing, see
[development.md](development.md).

## Data format

Two files work together: **[`portfolio.sample.json`](../src/main/resources/portfolio.sample.json)** is a
filled-in example — copy it and overwrite with your own data; **[`portfolio.schema.json`](../src/main/resources/portfolio.schema.json)**
is the contract that says what's valid — validate against it.

Your data must match the schema (JSON Schema 2020-12). The top level has five required keys — `profile`,
`experience`, `skills`, `projects`, `education`. `endDate` is nullable (`null` = current role / ongoing
study); dates are ISO-8601 (`YYYY-MM-DD`). Most editors (VS Code, IntelliJ) autocomplete and validate
against the schema if you add a `$schema` line pointing at it.

Validate before deploying:

```bash
# check-jsonschema (pipx install check-jsonschema)
check-jsonschema --schemafile src/main/resources/portfolio.schema.json my-portfolio.json
```

## Where the data comes from

Resolved in priority order, so you can supply data without committing it:

| Source | How | When to use |
|---|---|---|
| `PORTFOLIO_DATA_JSON` | Inline JSON in an env var | PaaS / hosted (nothing touches disk) |
| `portfolio.data.location` | A Spring resource path, e.g. `file:/data/portfolio.json` | Mounted file, load without rebuild |
| `classpath:portfolio.json` | Your own bundled file (gitignored) | Local development |
| `classpath:portfolio.sample.json` | The shipped example | Fallback so a fresh clone boots |

For the env var, minify to one line:

```bash
jq -c . my-portfolio.json    # paste the result into PORTFOLIO_DATA_JSON
```

## Run it locally

**With Docker** (nothing else to install):

```bash
docker build -t portfolio-mcp .
docker run -p 8080:8080 portfolio-mcp
```

**Or with Java 21 + Maven:**

```bash
mvn spring-boot:run
```

Either way the server is at `http://localhost:8080/mcp`, with a health check at
`http://localhost:8080/actuator/health`.

## Connect an AI client

The transport is **Streamable HTTP** at `POST /mcp`. The quickest way to explore it is the
[MCP Inspector](https://github.com/modelcontextprotocol/inspector):

```bash
npx @modelcontextprotocol/inspector
```

In the UI: **Transport type** → `Streamable HTTP`, **URL** → `http://localhost:8080/mcp` (or your
deployed URL) → **Connect**. You can now browse and call every tool, resource, and prompt.

To wire it into an MCP client that reads a config file, point it at the same URL with the Streamable
HTTP transport.

## Quick check without a client

```bash
curl localhost:8080/actuator/health

curl -s -X POST localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}'
```

The `Accept` header must include both `application/json` and `text/event-stream` — the stateless
transport requires it. `/mcp` is **POST-only**; opening it in a browser returns 405 by design.
