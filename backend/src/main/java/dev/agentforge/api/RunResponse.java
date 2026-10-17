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
        List<RunEvent> events) {
    public RunResponse {
        events = List.copyOf(events);
    }
}
