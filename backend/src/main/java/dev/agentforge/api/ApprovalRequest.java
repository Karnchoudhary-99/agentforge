package dev.agentforge.api;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ApprovalRequest(
        @NotNull UUID runId,
        @NotBlank String toolName,
        @NotNull JsonNode input) {}
