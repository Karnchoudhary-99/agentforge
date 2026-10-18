# Architecture

## Request path

React + TypeScript UI → Spring Boot REST API → request validation → agent orchestrator → structured plan → independent tool execution policy → approval gate (where required) → registered tool executor / server-side Spring AI provider → output validation → persisted run/events → UI timeline.

## Tool execution safety boundary

The server-owned `ToolRegistry` determines which tool implementations exist. The independent `ToolExecutionPolicy` then default-denies any registered tool not explicitly named in `AGENTFORGE_ALLOWED_TOOLS`. Listing and execution use the same policy so denied tools are not advertised. Tool input is measured as serialized UTF-8 JSON before execution; the default maximum is 8 KiB. Calls run in a fixed-size worker pool with a bounded queue and a deadline; saturation is rejected rather than creating unbounded threads. A deadline cancels the future with thread interruption. Tool implementations must cooperate with interruption, since the JVM cannot safely force-stop non-cooperative code. Automatic retries are disabled until idempotency semantics are explicit.

Current development defaults: allow only `text.word-count`, 8 KiB input, 2-second deadline, 4 concurrent workers, queue capacity 16, and retry budget zero. Invalid limit configuration fails startup. Tool execution and approval routes are protected by the `AGENTFORGE_API_TOKEN` shared-secret gate and fail closed when it is unset. This is not production identity or role-based authorization; do not expose the service publicly.

## Trust boundaries

- The LLM is an untrusted planner, not an authorization authority.
- Only explicitly registered and policy-allowed tools can execute; model output never maps directly to shell, filesystem, raw SQL, or arbitrary URLs.
- Validate structured input on the server. Apply step count, timeout, payload-size, concurrency, and retry limits.
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
