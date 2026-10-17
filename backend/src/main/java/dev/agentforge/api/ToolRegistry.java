package dev.agentforge.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Server-owned allowlist. User/model input can select only capabilities registered here. */
@Service
public class ToolRegistry {
    private final Map<String, AgentTool> tools;

    public ToolRegistry(List<AgentTool> registeredTools) {
        Map<String, AgentTool> indexed = new LinkedHashMap<>();
        for (AgentTool tool : registeredTools) {
            String name = tool.definition().name();
            if (indexed.putIfAbsent(name, tool) != null) {
                throw new IllegalStateException("Duplicate registered tool name: " + name);
            }
        }
        this.tools = Map.copyOf(indexed);
    }

    public List<ToolDefinition> list() {
        return tools.values().stream()
                .map(AgentTool::definition)
                .sorted(Comparator.comparing(ToolDefinition::name))
                .toList();
    }

    public ToolExecutionResponse execute(String name, JsonNode input) {
        AgentTool tool = tools.get(name);
        if (tool == null) {
            throw new ToolNotFoundException(name);
        }
        return tool.execute(input);
    }
}
