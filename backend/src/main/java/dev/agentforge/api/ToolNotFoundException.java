package dev.agentforge.api;

public class ToolNotFoundException extends RuntimeException {
    public ToolNotFoundException(String name) {
        super("Tool is not registered or allowlisted: " + name);
    }
}
