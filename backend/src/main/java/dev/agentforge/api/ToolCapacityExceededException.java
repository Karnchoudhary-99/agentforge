package dev.agentforge.api;

public class ToolCapacityExceededException extends RuntimeException {
    public ToolCapacityExceededException() {
        super("Tool execution capacity is temporarily full; retry later.");
    }
}
