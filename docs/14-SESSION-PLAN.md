# AgentForge — 14-session delivery plan

**Target:** a launch-ready, portfolio-quality MVP by Session 14, with reproducible setup, tests, documentation, security review, and green CI. Session count tracks completed work sessions; it is not a promise of unattended background execution.

## Release scope
- Single workspace, curated workflow templates, one external LLM provider.
- Explicit tool registry and server-side policy enforcement.
- Approval gates for sensitive actions; bounded steps/timeouts/retries.
- Persisted runs/events and a visible execution timeline.
- Evaluation fixtures and deterministic mock provider for CI.
- No arbitrary code/shell execution, unrestricted network tools, multi-tenant billing, or marketplace.

## Sessions
1. **Foundation:** repository, architecture, backend/frontend skeleton, local services, CI, health endpoint.
2. **Vertical slice:** agent/run domain model, API contract, UI create/run journey, mock execution end-to-end.
3. **Agent runtime:** structured task plan, bounded orchestration, state transitions.
4. **Tool registry:** typed tool schemas, allowlist, validation, safe example tools.
5. **Safety I (complete):** independent default-deny tool policy, serialized input budget, bounded executor pool/queue, execution deadlines, interruption-based timeout cancellation, zero automatic retries, negative tests.
6. **Safety II + access control (complete):** server-side shared-token gate for tool execution and approval routes; fail-closed configuration; run-bound approval requests; single-use approve/reject/execute; minimal audit events; negative token tests and approval replay tests. This remains a local MVP gate, not production identity.
7. **Persistence (complete):** PostgreSQL run/event schema via Flyway, JPA repositories, transactional run + event writes, persisted plan snapshots, ordered timeline reads, PostgreSQL Testcontainers integration tests.
8. **Observability (complete):** richer run details and failure reasons, timeline semantics, token/cost estimates when provider data exists, and safe diagnostic output.
9. **Workflow UI (in progress):** React Flow editor, bounded node/edge validation, accessible node inspector, and explicit client-draft/server-policy boundary.
10. **Templates and live timeline:** reusable templates, SSE or polling fallback, loading/error/empty states.
11. **Spring AI integration:** server-side provider configuration, structured output, provider failure handling.
12. **Evaluation and security tests:** golden test cases, deterministic mocks, prompt-injection/tool-boundary tests, output validation.
13. **Release candidate:** E2E tests, dependency/security review, Docker clean-start, OpenAPI, demo data/screenshots.
14. **Launch gate:** clean-machine install, full regression, CI green, docs and demo finalized, tagged release if all gates pass.

## Completion gate per session
- Planned code/docs are committed to the AgentForge repository.
- Unit/integration checks run; failures are fixed and rerun.
- Frontend build and applicable E2E checks run.
- GitHub Actions is checked; a session is not called complete with a failing required job.
- Status report states what is verified, what remains, and the next session.

If an external dependency or credential is unavailable, a deterministic mock keeps CI and demos testable; live-provider verification is reported separately. Never fake a green status.
