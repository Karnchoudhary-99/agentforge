package dev.agentforge.api;

import java.time.Instant;
import java.util.ArrayList;
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
    private final TaskPlanner taskPlanner;

    public RunService(TaskPlanner taskPlanner) {
        this.taskPlanner = taskPlanner;
    }

    public RunResponse create(CreateRunRequest request) {
        Instant createdAt = Instant.now();
        UUID id = UUID.randomUUID();
        RunStatus status = RunStatus.QUEUED;
        List<RunEvent> events = new ArrayList<>();
        events.add(new RunEvent(createdAt, "run.created", "Run accepted and placed in the queue."));

        status = status.transitionTo(RunStatus.RUNNING);
        events.add(new RunEvent(createdAt.plusMillis(1), "run.started", "Bounded mock runtime started."));

        List<PlanStep> plan = taskPlanner.createPlan(request.workflowName(), request.task());
        events.add(new RunEvent(createdAt.plusMillis(2), "plan.prepared",
                "Prepared " + plan.size() + " bounded steps; no model or tools were invoked."));

        status = status.transitionTo(RunStatus.SUCCEEDED);
        events.add(new RunEvent(createdAt.plusMillis(3), "run.succeeded", "Deterministic mock execution completed."));

        RunResponse run = new RunResponse(
                id,
                request.workflowName().trim(),
                request.task().trim(),
                status.name().substring(0, 1) + status.name().substring(1).toLowerCase(),
                "Mock execution completed safely for the submitted task. No external model or tools were invoked.",
                createdAt,
                plan,
                List.copyOf(events));
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
