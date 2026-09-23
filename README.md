# portfolio-mcp

A reusable [Model Context Protocol](https://modelcontextprotocol.io) (MCP) server for professional
portfolios. Point an MCP-compatible AI assistant at it and it can read a portfolio through a **fixed,
deterministic** interface — the same six tools, six resources, and two prompts regardless of whose
data is loaded.

Built with Kotlin, Spring Boot, and Spring AI on Java 21. Stateless, no database, runs anywhere as a container.

> **You don't need to deploy anything to use this.** Clone it, drop in your data, run it locally, and
> connect your AI client — see [docs/usage.md](docs/usage.md). Hosting it is optional.

> **Live instance:** a running example serving the author's own portfolio is at
> **`https://jhubie.thetiongsons.com/mcp`** (Streamable HTTP). Point an MCP client at it to see the
> interface in action. To serve *your own* data, run your own instance (below) — this URL is one
> person's deployment, not a shared multi-user service.

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

## Deploy your own

One instance serves one person's portfolio. To run yours, deploy your own copy — you get an isolated
instance with your own data and public URL.

[![Deploy on Railway](https://railway.com/button.svg)](https://railway.com/deploy/7vUmWu?referralCode=IeiZ1B&utm_medium=integration&utm_source=template&utm_campaign=generic)

1. Click the button — Railway builds this repo's `Dockerfile`.
2. When prompted, set **`PORTFOLIO_DATA_JSON`** to your portfolio JSON (the shape is defined by
   [`portfolio.schema.json`](src/main/resources/portfolio.schema.json)). It takes precedence over the
   bundled sample, so your data never needs to be committed.
3. Under **Networking**, generate a domain (or add a custom one). Your server is live at
   `https://<your-domain>/mcp`.

No database, no secrets — the same image runs on Fly, Cloud Run, or any container host.

## Run it yourself

Prefer local, Docker, or another host? See **[docs/usage.md](docs/usage.md)** for the data format,
running the server, and connecting an MCP client. For project internals and contributing, see
**[docs/development.md](docs/development.md)**.

## Security

This server exposes **public, read-only** portfolio data over an **unauthenticated** endpoint by
design. Only include information you are happy to publish — there are no secrets, no mutations, and no
database. If you later expose private data, put authentication in front of the endpoint.

## License

MIT
