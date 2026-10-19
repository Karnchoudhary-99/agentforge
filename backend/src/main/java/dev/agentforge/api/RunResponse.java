package dev.agentforge.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RunResponse(
        UUID id,
        String workflowName,
        String task,
        String status,
        String summary,
        Instant createdAt,
        List<PlanStep> plan,
        List<RunEvent> events,
        Long durationMs,
        String failureReason) {
    public RunResponse {
        plan = List.copyOf(plan);
        events = List.copyOf(events);
        if (durationMs != null && durationMs < 0) {
            throw new IllegalArgumentException("durationMs cannot be negative");
        }
        if (failureReason != null && failureReason.length() > 240) {
            throw new IllegalArgumentException("failureReason cannot exceed 240 characters");
        }
    }
}
