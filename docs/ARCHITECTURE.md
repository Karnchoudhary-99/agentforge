# Architecture

## Request path

React + TypeScript UI → Spring Boot REST API → validation → bounded planner → server-owned tool policy → approval gate when required → registered tool executor / server-side Spring AI provider → output validation → PostgreSQL run/event repositories → UI timeline.

## Persistence boundary

- `agent_runs` stores run ID, workflow/task snapshot, status, summary, creation time, and serialized plan snapshot.
- `run_events` stores each timeline event as a row linked to its run with a foreign key and cascade delete.
- Flyway migrations are the schema source of truth; Hibernate runs in `validate` mode and does not create or mutate tables.
- Saving a run and its initial events is transactional. If event insertion fails, the run insert rolls back.
- Run lists are ordered newest-first and include ordered events. Individual reads load only one run's events.
- PostgreSQL is required; there is no silent volatile-storage fallback. Approval decisions and audit events remain in-memory for now.

## Tool execution safety

The server-owned `ToolRegistry` determines which implementations exist. The independent `ToolExecutionPolicy` default-denies any registered tool not explicitly named in `AGENTFORGE_ALLOWED_TOOLS`. Listing and execution share the same policy. Serialized UTF-8 input is capped at 8 KiB; calls run in a fixed worker pool with a bounded queue and a deadline. Saturation is rejected rather than creating unbounded threads. Deadlines interrupt the worker; tool code must cooperate. Automatic retries remain disabled until idempotency is explicit.

Defaults: allow only `text.word-count`, 8 KiB input, 2-second deadline, 4 workers, queue capacity 16, and zero retries. Tool execution and approval routes require the server-side `AGENTFORGE_API_TOKEN` via a shared-token gate. It is not production identity or role-based authorization; do not expose the service publicly.

## Trust boundaries

- The LLM is an untrusted planner, never an authorization authority.
- Model output cannot map directly to shell, arbitrary code, filesystem access, raw SQL, or arbitrary URLs.
- Validate all structured inputs and bound steps, time, payloads, concurrency, and retries.
- Require explicit human approval for sensitive side effects.
- Keep provider credentials server-side; never expose them to React or logs.
- Use deterministic mocks in CI so tests do not require paid provider credentials.

## Module boundaries

- `api`: REST controllers and DTOs.
- `agent`: orchestration, planning, state machine.
- `policy`: allowlist, validation, approval decisions and limits.
- `tools`: registered least-privilege tools.
- `provider`: Spring AI and mock adapters.
- `persistence`: run/event repositories and migrations.
- `evaluation`: fixtures, scoring and regression tests.

Introduce modules as behavior becomes testable; avoid speculative abstractions before the vertical slice exists.
