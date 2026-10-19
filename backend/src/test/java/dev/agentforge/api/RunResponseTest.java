package dev.agentforge.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RunResponseTest {
    @Test
    void rejectsRawFailureTextInsteadOfSafeDiagnosticCode() {
        assertThatThrownBy(() -> new RunResponse(UUID.randomUUID(), "workflow", "task", "Failed",
                "Safe summary", Instant.EPOCH, List.of(), List.of(), null,
                "java.lang.IllegalStateException: secret token=abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("failureReason must be a safe diagnostic code");
    }

    @Test
    void rejectsNegativeDuration() {
        assertThatThrownBy(() -> new RunResponse(UUID.randomUUID(), "workflow", "task", "Succeeded",
                "Safe summary", Instant.EPOCH, List.of(), List.of(), -1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("durationMs cannot be negative");
    }
}
