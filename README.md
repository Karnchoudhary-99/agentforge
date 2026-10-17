# AgentForge

**An observable, policy-bounded AI agent platform with human approval gates.** AgentForge helps teams define and run controlled AI workflows with registered tools, explicit approval for sensitive actions, durable execution history, and evaluation. It is intentionally more than a chat UI: the application, not the model, owns tool permissions and execution.

> **Project status:** Session 2 — end-to-end deterministic mock run slice. The 14-session MVP plan is tracked in [docs/14-SESSION-PLAN.md](docs/14-SESSION-PLAN.md). Release readiness will only be claimed after required tests, security checks, setup verification, and GitHub Actions are green.

## MVP promise

By the end of Session 14, the intended release will provide:
- A React + TypeScript workspace to create and run bounded agent workflows.
- A Java 21 / Spring Boot API with a clear agent orchestration boundary.
- Spring AI integration with one external model provider, configured only on the server.
- Explicit tool allowlists, schema validation, execution limits, and human approval for sensitive actions.
- PostgreSQL-backed run history and execution events.
- A live execution timeline, reproducible evaluation fixtures, and a documented local deployment.
- Automated backend/frontend CI and end-to-end verification.

Scope is deliberately bounded: one workspace, curated tools, one model provider for the first release. No arbitrary code execution, unrestricted shell, open-ended network access, billing, or plugin marketplace.

## Technology

- Java 21, Spring Boot, Spring AI, Bean Validation
- React, TypeScript, Vite, React Flow
- PostgreSQL; optional Redis only if a measured requirement justifies it
- JUnit 5, Mockito, Testcontainers, Vitest, Playwright
- Docker Compose, GitHub Actions, OpenAPI

## Architecture and safety

The model proposes structured actions; server-side policy validates every action before execution. Tools are explicitly registered and allowlisted. Sensitive actions pause for human approval. Tool inputs are validated, execution has bounded steps/timeouts/retries, and results are recorded as evidence. Provider keys remain in backend environment variables and must never be placed in browser code, checked into Git, or included in logs.

See [architecture](docs/ARCHITECTURE.md), [session plan](docs/14-SESSION-PLAN.md), [project switch record](docs/PROJECT-SWITCH-RECORD.md), and [API reference](docs/API.md).

## Local development

Prerequisites: JDK 21, Maven 3.9+, Node.js 22+, npm, and Docker Compose.

1. Copy `.env.example` to `.env` and set a model API key only if using live model calls.
2. Start services: `docker compose up -d postgres`.
3. Run the API: `cd backend && mvn spring-boot:run`.
4. Run the UI: `cd frontend && npm ci && npm run dev`.
5. Open the Vite URL (usually `http://localhost:5173`). The Vite dev server proxies `/api/*` to the API on port 8080.
6. Health endpoint: `GET http://localhost:8080/api/health`.

The mock run endpoints are documented in [docs/API.md](docs/API.md). Runs are in-memory only until the persistence session; restarting the API clears them. The mock executor never calls an external model or tools.

Never commit `.env` or credentials. The exact available endpoints and commands will evolve as the MVP is implemented; consult this README and the session status report for the current verified state.

## Development quality gate

A session is not complete until its planned changes are present, tests and verification have been run, failures have been fixed or explicitly blocked by a documented external dependency, and CI is green. A green CI badge alone is not a substitute for functional/security review.
