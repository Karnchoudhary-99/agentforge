# AgentForge API — bounded runtime (Session 5)

Base URL for local development: http://localhost:8080. The Vite dev server proxies /api/* to the API on port 8080.

## Create a mock run

POST /api/runs accepts JSON with workflowName and task.

- workflowName: required, non-blank, at most 80 characters.
- task: required, non-blank, at most 1,000 characters.
- Success: 201 Created, a Location: /api/runs/{uuid} header, and the run response.
- The deterministic runtime transitions through QUEUED → RUNNING → SUCCEEDED.
- The response contains a fixed three-step plan and four timeline events: run.created, run.started, plan.prepared, run.succeeded.
- Invalid request bodies return 400 Bad Request.
- This remains a mock runtime: it does not call an LLM, access the network, or perform external side effects.

## Tool registry and execution policy

- GET /api/tools lists only tools enabled by the server-side `agentforge.tools.allowed` allowlist.
- POST /api/tools/{toolName}/execute executes an enabled, registered tool with a JSON request body.
- The only registered example is `text.word-count`. Its input must be an object containing exactly one `text` string, non-blank and at most 1,000 characters. Extra fields, missing fields, wrong types, blank strings, and oversized strings return 400 Bad Request.
- The default allowed tools value is `text.word-count`; override with the server environment variable `AGENTFORGE_ALLOWED_TOOLS` as a comma-separated list of explicitly trusted tool names. An empty allowlist fails application startup (fail-closed).
- A registered tool that is not allowed returns 403 Forbidden. An unknown/unregistered tool returns 404 Not Found.
- The serialized JSON input budget defaults to 8 KiB (`AGENTFORGE_TOOL_MAX_INPUT_BYTES`); exceeding it returns 413 Payload Too Large before the tool runs.
- Execution defaults to a 2,000 ms deadline (`AGENTFORGE_TOOL_TIMEOUT_MS`). Timed-out tasks are interrupted and return 504 Gateway Timeout. Tool code must cooperate with interruption; Java cannot safely force-stop code that ignores interrupts.
- Concurrency defaults to 4 active executions and a queue of 16 (`AGENTFORGE_TOOL_MAX_CONCURRENT`, `AGENTFORGE_TOOL_QUEUE_CAPACITY`). Saturation rejects new work with 503 Service Unavailable rather than spawning unbounded threads.
- Automatic retries are intentionally disabled (retry budget = 0) because retrying a tool may duplicate side effects. A future retry policy must require explicit idempotency semantics and per-tool approval.
- Configuration values for timeout, concurrency, queue capacity and input size must be positive; invalid resource-limit configuration fails application startup.
- The example tool only counts words and characters in supplied text. No shell, arbitrary code, filesystem, or network tool exists.
- This API still has no authentication/authorization layer. It is for trusted local development only and must not be exposed to untrusted users or the public internet until access control is implemented and tested.

## State transition rules

Allowed transitions:
- QUEUED → RUNNING or QUEUED → CANCELLED
- RUNNING → SUCCEEDED, RUNNING → FAILED, or RUNNING → CANCELLED
- SUCCEEDED, FAILED, and CANCELLED are terminal states.

Illegal transitions throw IllegalStateException and are covered by unit tests. The current mock endpoint completes synchronously; run cancellation and asynchronous run execution are not wired into run HTTP endpoints yet. Tool deadlines cancel the worker Future with interruption.

## List and retrieve runs

- GET /api/runs returns runs newest first.
- GET /api/runs/{uuid} returns a run or 404 Not Found if unknown.
- Run records remain in process memory and are cleared when the backend restarts. Durable persistence is planned for Session 7.

## Run response shape

The response includes id, workflowName, task, status, summary, createdAt, plan (ordered PlanStep objects), and events (timestamped RunEvent objects). Plan steps expose order, title, description, and status. The current mock plan always contains three bounded steps; each is marked COMPLETED because execution is synchronous and deterministic.
