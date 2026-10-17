package dev.agentforge.api;

import java.util.Map;

public record ToolDefinition(String name, String description, Map<String, Object> inputSchema) {
    public ToolDefinition {
        inputSchema = Map.copyOf(inputSchema);
    }
}
