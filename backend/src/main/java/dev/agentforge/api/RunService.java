package dev.agentforge.api;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class RunService {
    private final Map<UUID, RunResponse> runs = new ConcurrentHashMap<>();

    public RunResponse create(CreateRunRequest request) {
        Instant createdAt = Instant.now();
        UUID id = UUID.randomUUID();
        List<RunEvent> events = List.of(
                new RunEvent(createdAt, "run.created", "Run accepted by AgentForge."),
                new RunEvent(createdAt.plusMillis(1), "mock.completed",
                        "Deterministic mock completed; no model or external tools were called."));
        RunResponse run = new RunResponse(
                id,
                request.workflowName().trim(),
                request.task().trim(),
                "Succeeded",
                "Mock execution completed safely for the submitted task. No external model or tools were invoked.",
                createdAt,
                events);
        runs.put(id, run);
        return run;
    }

    public List<RunResponse> list() {
        return runs.values().stream()
                .sorted(Comparator.comparing(RunResponse::createdAt).reversed())
                .toList();
    }

    public Optional<RunResponse> find(UUID id) {
        return Optional.ofNullable(runs.get(id));
    }
}
