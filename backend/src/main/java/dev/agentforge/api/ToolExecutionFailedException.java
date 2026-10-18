package dev.agentforge.api;

public class ToolExecutionFailedException extends RuntimeException {
    public ToolExecutionFailedException() {
        super("Tool execution failed.");
    }
}
