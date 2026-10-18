package dev.agentforge.api;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Server-owned default-deny policy, independent of the tool registry itself. */
@Component
public class ToolExecutionPolicy {
    private final Set<String> allowedToolNames;
    private final int maxInputBytes;

    public ToolExecutionPolicy(
            @Value("${agentforge.tools.allowed:text.word-count}") String allowedTools,
            @Value("${agentforge.tools.max-input-bytes:8192}") int maxInputBytes) {
        this.allowedToolNames = Arrays.stream(allowedTools.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        if (this.allowedToolNames.isEmpty()) {
            throw new IllegalStateException("At least one explicitly allowed tool must be configured.");
        }
        if (maxInputBytes < 1) {
            throw new IllegalStateException("Tool input byte limit must be positive.");
        }
        this.maxInputBytes = maxInputBytes;
    }

    public boolean isAllowed(String toolName) {
        return allowedToolNames.contains(toolName);
    }

    public void assertAllowed(String toolName) {
        if (!isAllowed(toolName)) {
            throw new ToolPolicyDeniedException(toolName);
        }
    }

    public int maxInputBytes() {
        return maxInputBytes;
    }
}
