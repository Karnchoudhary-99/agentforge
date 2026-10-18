package dev.agentforge.api;

public class ApprovalNotFoundException extends RuntimeException {
    public ApprovalNotFoundException(String message) { super(message); }
}
