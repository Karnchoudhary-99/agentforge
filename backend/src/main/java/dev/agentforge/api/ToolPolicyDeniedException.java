package dev.agentforge.api;

public class ToolPolicyDeniedException extends RuntimeException {
    public ToolPolicyDeniedException(String toolName) {
        super("Tool is registered but denied by execution policy: " + toolName);
    }
}
