# Architecture

## Request path

React + TypeScript UI → Spring Boot REST API → request validation → agent orchestrator → structured plan → policy and tool-input validation → approval gate (where required) → registered tool executor / server-side Spring AI provider → output validation → persisted run/events → UI timeline.

## Trust boundaries
- The LLM is an untrusted planner, not an authorization authority.
- Only explicitly registered tools can execute; model output never maps directly to shell, filesystem, raw SQL, or arbitrary URLs.
- Validate structured input on the server. Apply step count, timeout, payload-size, and retry limits.
- Require explicit human approval for side effects. Approval is bound to a run/action and expires according to server policy.
- Persist run state transitions and tool results for auditability; redact secrets and sensitive headers.
- Keep provider credentials server-side in environment/deployment secrets. Never expose them to React or logs.
- Use deterministic fake providers and tools in CI so tests do not need paid API credentials.

## Initial module boundaries
- `api`: versioned REST controllers and request/response DTOs.
- `agent`: orchestration, run state machine, plan parsing.
- `policy`: tool allowlist, validation, approval decisions and limits.
- `tools`: typed, registered tools with least privilege.
- `provider`: Spring AI adapter and mock adapter.
- `persistence`: run/event repositories and migrations.
- `evaluation`: fixtures, scoring and regression tests.

Modules are introduced incrementally as the behavior becomes testable; avoid speculative abstractions before the vertical slice exists.
