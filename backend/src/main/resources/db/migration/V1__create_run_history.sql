CREATE TABLE agent_runs (
    id UUID PRIMARY KEY,
    workflow_name VARCHAR(80) NOT NULL,
    task VARCHAR(1000) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('Queued', 'Running', 'Succeeded', 'Failed', 'Cancelled')),
    summary TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    plan_json TEXT NOT NULL
);

CREATE INDEX idx_agent_runs_created_at ON agent_runs (created_at DESC);

CREATE TABLE run_events (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES agent_runs(id) ON DELETE CASCADE,
    event_timestamp TIMESTAMPTZ NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    message TEXT NOT NULL
);

CREATE INDEX idx_run_events_run_timestamp ON run_events (run_id, event_timestamp, id);
