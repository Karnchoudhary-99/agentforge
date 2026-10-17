# AgentForge API — bounded runtime (Session 3)

Base URL for local development: http://localhost:8080. The Vite dev server proxies /api/* to the API on port 8080.

## Create a mock run

POST /api/runs accepts JSON with workflowName and task.

- workflowName: required, non-blank, at most 80 characters.
- task: required, non-blank, at most 1,000 characters.
- Success: 201 Created, a Location: /api/runs/{uuid} header, and the run response.
- The deterministic runtime transitions through QUEUED → RUNNING → SUCCEEDED.
- The response contains a fixed three-step plan and four timeline events: run.created, run.started, plan.prepared, run.succeeded.
- Invalid request bodies return 400 Bad Request.
- This remains a mock runtime: it does not call an LLM, execute tools, access the network, or perform side effects.

## State transition rules

Allowed transitions:
- QUEUED → RUNNING or QUEUED → CANCELLED
- RUNNING → SUCCEEDED, RUNNING → FAILED, or RUNNING → CANCELLED
- SUCCEEDED, FAILED, and CANCELLED are terminal states.

Illegal transitions throw IllegalStateException and are covered by unit tests. The current mock endpoint completes synchronously; cancellation and asynchronous execution are not wired into HTTP endpoints yet.

## List and retrieve runs

- GET /api/runs returns runs newest first.
- GET /api/runs/{uuid} returns a run or 404 Not Found if unknown.
- Run records remain in process memory and are cleared when the backend restarts. Durable persistence is planned for Session 7.

## Run response shape

The response includes id, workflowName, task, status, summary, createdAt, plan (ordered PlanStep objects), and events (timestamped RunEvent objects). Plan steps expose order, title, description, and status. The current mock plan always contains three bounded steps; each is marked COMPLETED because execution is synchronous and deterministic.
