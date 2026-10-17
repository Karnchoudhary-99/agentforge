package dev.agentforge.api;

import java.time.Instant;

public record RunEvent(Instant timestamp, String type, String message) {
}
