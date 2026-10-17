# AgentForge API — Session 2 mock vertical slice

Base URL for local development: `http://localhost:8080`. The Vite dev server proxies `/api/*` to this backend on port 8080.

## Create a mock run

`POST /api/runs` accepts JSON:

```json
{
  "workflowName": "Research brief",
  "task": "Summarize the main risks in this supplied task"
}
```

- `workflowName`: required, non-blank, at most 80 characters.
- `task`: required, non-blank, at most 1,000 characters.
- Success: `201 Created`, a `Location: /api/runs/{uuid}` header, and the run response.
- The deterministic mock immediately returns `Succeeded` and a two-event timeline. It does not call an LLM, execute tools, access the network, or perform side effects.
- Invalid request bodies return `400 Bad Request`.

## List runs

`GET /api/runs` returns runs newest first. Runs are held in process memory for this session only; restarting the API clears them. Durable persistence is planned for Session 7.

## Get one run

`GET /api/runs/{uuid}` returns the run or `404 Not Found` if the identifier is unknown.

## Run response shape

```json
{
  "id": "e2f2a6d2-1a4f-4a36-a3f2-56e7e98f7b9b",
  "workflowName": "Research brief",
  "task": "Summarize the main risks in this supplied task",
  "status": "Succeeded",
  "summary": "Mock execution completed safely for the submitted task. No external model or tools were invoked.",
  "createdAt": "2026-10-17T09:00:00Z",
  "events": [
    { "timestamp": "2026-10-17T09:00:00Z", "type": "run.created", "message": "Run accepted by AgentForge." },
    { "timestamp": "2026-10-17T09:00:00.001Z", "type": "mock.completed", "message": "Deterministic mock completed; no model or external tools were called." }
  ]
}
```

This API is an MVP vertical slice, not the final orchestration engine. Tool policies, approval decisions, persistence, cancellation, and live-provider integration are introduced in later sessions.
