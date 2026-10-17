package dev.agentforge.api;

import java.util.Map;

public record ToolExecutionResponse(String toolName, Map<String, Object> output) {
    public ToolExecutionResponse {
        output = Map.copyOf(output);
    }
}
