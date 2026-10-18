package dev.agentforge.api;

public class ToolExecutionTimeoutException extends RuntimeException {
    public ToolExecutionTimeoutException(long timeoutMillis) {
        super("Tool execution exceeded the " + timeoutMillis + " ms time limit.");
    }
}
