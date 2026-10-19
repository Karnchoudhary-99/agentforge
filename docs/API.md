# AgentForge API — bounded runtime and persisted history

Local base URL: `http://localhost:8080`. The Vite development server proxies `/api/*` to the backend.

## Runs

- `POST /api/runs` accepts `workflowName` (non-blank, max 80 chars) and `task` (non-blank, max 1,000 chars).
- Success is `201 Created` with a `Location: /api/runs/{uuid}` header and the run response.
- The deterministic mock transitions `QUEUED → RUNNING → SUCCEEDED`, returns a three-step plan, and records four events: `run.created`, `run.started`, `plan.prepared`, and `run.succeeded`.
- Invalid bodies return `400 Bad Request`.
- `GET /api/runs` returns persisted runs newest first; `GET /api/runs/{uuid}` returns the run with its plan and ordered timeline or `404 Not Found`.
- PostgreSQL is the source of truth. Flyway applies versioned migrations at startup; Hibernate validates the schema. There is no silent in-memory fallback if PostgreSQL is unavailable.

## Tool execution and approvals

- Tool execution and all `/api/approvals/**` routes require the `X-AgentForge-Token` header. Configure a strong backend-only `AGENTFORGE_API_TOKEN`; missing/wrong request tokens return `401`, and an unset server token fails closed with `503`.
- This shared-token gate is not production identity or role-based authorization. Keep the service on trusted local networks; use HTTPS and a proper identity provider before public deployment.
- `GET /api/tools` lists tools permitted by the server-side `AGENTFORGE_ALLOWED_TOOLS` allowlist.
- `POST /api/tools/{toolName}/execute` executes an allowed registered tool. The only example is `text.word-count`, requiring exactly one non-blank `text` string of at most 1,000 characters.
- Registered but disallowed tools return `403`; unknown tools return `404`; invalid inputs return `400`; serialized inputs over the default 8 KiB budget return `413`.
- Tool execution defaults to a 2,000 ms deadline, 4 workers, a queue of 16, and zero retries. Timeout returns `504`; saturation returns `503`. Tool implementations must cooperate with thread interruption.
- `POST /api/approvals` creates an approval bound to an existing run ID, exact tool name, and deep-copied input. `POST /api/approvals/{id}/approve` or `/reject` makes one decision; stale/repeated decisions return `409`. `POST /api/approvals/{id}/execute` consumes an approval before execution to prevent replay.
- Approval records and minimal audit events are still in-memory; they are not durable across restarts. Audit events exclude request payloads and secrets.

## Run response and state machine

A run response contains `id`, `workflowName`, `task`, `status`, `summary`, `createdAt`, `plan`, and `events`. Plan steps expose order, title, description, and status. The mock runtime is synchronous and does not invoke a live LLM or external tools.

Allowed transitions are `QUEUED → RUNNING/CANCELLED`, `RUNNING → SUCCEEDED/FAILED/CANCELLED`, and no transitions from terminal states. Full asynchronous run cancellation is not yet wired to the HTTP endpoints.
