package dev.agentforge.api;

import java.time.Instant;
import java.util.List;
import java.util.Set;
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
    private static final Set<String> SAFE_FAILURE_REASONS = Set.of(
            "TIMEOUT", "PROVIDER_UNAVAILABLE", "OUTPUT_INVALID", "POLICY_DENIED", "UNKNOWN");

    public RunResponse {
        plan = List.copyOf(plan);
        events = List.copyOf(events);
        if (durationMs != null && durationMs < 0) {
            throw new IllegalArgumentException("durationMs cannot be negative");
        }
        if (failureReason != null && !SAFE_FAILURE_REASONS.contains(failureReason)) {
            throw new IllegalArgumentException("failureReason must be a safe diagnostic code");
        }
    }
}
