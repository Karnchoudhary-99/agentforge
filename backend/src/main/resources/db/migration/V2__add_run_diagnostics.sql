ALTER TABLE agent_runs
    ADD COLUMN duration_ms BIGINT NULL,
    ADD COLUMN failure_reason VARCHAR(240) NULL;

ALTER TABLE agent_runs
    ADD CONSTRAINT chk_agent_runs_duration_nonnegative
    CHECK (duration_ms IS NULL OR duration_ms >= 0);
