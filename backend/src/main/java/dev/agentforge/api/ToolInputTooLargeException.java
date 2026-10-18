package dev.agentforge.api;

public class ToolInputTooLargeException extends RuntimeException {
    public ToolInputTooLargeException(int maxBytes) {
        super("Tool input exceeds the configured limit of " + maxBytes + " bytes.");
    }
}
