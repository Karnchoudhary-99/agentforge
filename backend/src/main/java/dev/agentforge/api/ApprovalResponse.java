package dev.agentforge.api;

import java.time.Instant;
import java.util.UUID;

public record ApprovalResponse(
        UUID id, UUID runId, String toolName, String status, Instant createdAt, Instant decidedAt) {}
