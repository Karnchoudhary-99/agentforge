package dev.agentforge.api;

import com.fasterxml.jackson.databind.JsonNode;

/** A server-registered capability with an explicit input schema and bounded execution contract. */
public interface AgentTool {
    ToolDefinition definition();

    ToolExecutionResponse execute(JsonNode input);
}
