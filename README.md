# AgentForge

An observable, policy-bounded AI-agent MVP with server-owned tool permissions, human approval gates, and durable run history.

> **Status:** Session 7 is complete and verified. Session 8 adds persisted run diagnostics with safe failure classifications; see [the 14-session plan](docs/14-SESSION-PLAN.md), [Session 7 report](DAY-7-STATUS.md), and [Session 8 report](DAY-8-STATUS.md). Launch readiness will only be claimed after the final clean-install and release gates pass.

## MVP scope

- Java 21, Spring Boot, Spring AI (server-side provider), React, TypeScript, Vite, and React Flow.
- Explicit server-side tool registry and default-deny allowlist.
- Bounded payloads, concurrency, queue, and execution deadlines; retries disabled by default.
- Run-bound, single-use human approvals.
- PostgreSQL run history, ordered timeline events, and nullable diagnostic fields versioned by Flyway.
- Safe failure classification codes; raw exception messages are rejected. Mock mode reports duration and model usage as unavailable rather than inventing estimates.
- Deterministic mocks and automated backend/frontend checks.

No arbitrary shell/code execution, unrestricted network tools, billing, or marketplace are in scope.

## Safety boundaries

Only server-registered and policy-allowed tools can execute. The example tool is `text.word-count`; no shell, filesystem, or arbitrary network tool exists. Tool execution and approval endpoints require the server-side `AGENTFORGE_API_TOKEN` via the `X-AgentForge-Token` header. This is a minimal shared-secret gate, not user identity or role-based authorization. Do not expose the service publicly.

Run records, plan snapshots, and timeline events are stored in PostgreSQL. Approval decisions and audit events remain in-memory until a later hardening session. Provider credentials must stay in backend environment configuration and must never be committed or logged.

## Local development

Prerequisites: JDK 21, Maven 3.9+, Node.js 22+, npm, and Docker Compose.

1. Copy `.env.example` to `.env`.
2. From the repository root, load local variables into the current shell: `set -a; . ./.env; set +a`.
3. Set a fresh local access token: `export AGENTFORGE_API_TOKEN="$(openssl rand -hex 32)"`. Never commit that value.
4. Start PostgreSQL: `docker compose up -d postgres`.
5. Start the API in the same shell: `cd backend && mvn spring-boot:run`. Flyway applies migrations; Hibernate validates the schema.
6. In another terminal, run `cd frontend && npm ci && npm run dev`.
7. Open the Vite URL (usually `http://localhost:5173`). Health endpoint: `GET http://localhost:8080/api/health`.

For a live OpenAI provider in the later Spring AI session, export `SPRING_AI_OPENAI_API_KEY` in the backend shell. The current run endpoint remains a deterministic mock and makes no external model calls, so duration, token usage, and cost are not measured.

## Quality gate

A session is complete only after its planned code/docs, backend and frontend checks, actual GitHub Actions results/logs, and progress report are verified. CI green does not by itself mean the product is launch-ready.
