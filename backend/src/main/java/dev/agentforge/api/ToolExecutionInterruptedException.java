package dev.agentforge.api;

public class ToolExecutionInterruptedException extends RuntimeException {
    public ToolExecutionInterruptedException() {
        super("Tool execution was interrupted.");
    }
}
