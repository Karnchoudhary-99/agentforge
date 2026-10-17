package dev.agentforge.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRunRequest(
        @NotBlank @Size(max = 80) String workflowName,
        @NotBlank @Size(max = 1000) String task) {
}
