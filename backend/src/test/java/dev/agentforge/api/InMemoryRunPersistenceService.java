package dev.agentforge.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Test-only store used by unit tests that exercise approvals without starting PostgreSQL. */
final class InMemoryRunPersistenceService extends RunPersistenceService {
    private final Map<UUID, RunResponse> rows = new HashMap<>();

    InMemoryRunPersistenceService() {
        super(null, null, null);
    }

    @Override public void save(RunResponse run) { rows.put(run.id(), run); }
    @Override public List<RunResponse> list() { return List.copyOf(rows.values()); }
    @Override public Optional<RunResponse> find(UUID id) { return Optional.ofNullable(rows.get(id)); }
}
