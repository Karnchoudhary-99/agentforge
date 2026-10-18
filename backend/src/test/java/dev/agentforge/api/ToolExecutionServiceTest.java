package dev.agentforge.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class ToolExecutionServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deniesRegisteredToolThatIsNotInIndependentPolicyAllowlist() throws Exception {
        ToolRegistry registry = new ToolRegistry(java.util.List.of(new WordCountTool()));
        ToolExecutionPolicy policy = new ToolExecutionPolicy("other.tool", 8192);
        try (ToolExecutionService service = new ToolExecutionService(
                registry, policy, objectMapper, 500, 1, 1)) {
            assertThrows(ToolPolicyDeniedException.class,
                    () -> service.execute("text.word-count", objectMapper.readTree("""
                            {"text":"hello"}
                            """)));
            assertTrue(service.listAllowedTools().isEmpty());
        }
    }

    @Test
    void rejectsPayloadAboveByteBudgetBeforeToolExecution() throws Exception {
        ToolRegistry registry = new ToolRegistry(java.util.List.of(new WordCountTool()));
        ToolExecutionPolicy policy = new ToolExecutionPolicy("text.word-count", 32);
        try (ToolExecutionService service = new ToolExecutionService(
                registry, policy, objectMapper, 500, 1, 1)) {
            assertThrows(ToolInputTooLargeException.class,
                    () -> service.execute("text.word-count", objectMapper.readTree("""
                            {"text":"this payload is definitely larger than thirty two bytes"}
                            """)));
        }
    }

    @Test
    void cancelsTimedOutExecutionAndDoesNotRetryIt() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        AtomicBoolean invoked = new AtomicBoolean();
        AgentTool slowTool = new AgentTool() {
            @Override
            public ToolDefinition definition() {
                return new ToolDefinition("test.slow", "Test-only slow tool", Map.of("type", "object"));
            }

            @Override
            public ToolExecutionResponse execute(com.fasterxml.jackson.databind.JsonNode input) {
                invoked.set(true);
                started.countDown();
                try {
                    Thread.sleep(5_000);
                } catch (InterruptedException exception) {
                    interrupted.countDown();
                    Thread.currentThread().interrupt();
                }
                return new ToolExecutionResponse("test.slow", Map.of("done", true));
            }
        };
        ToolRegistry registry = new ToolRegistry(java.util.List.of(slowTool));
        ToolExecutionPolicy policy = new ToolExecutionPolicy("test.slow", 8192);
        try (ToolExecutionService service = new ToolExecutionService(
                registry, policy, objectMapper, 50, 1, 1)) {
            assertThrows(ToolExecutionTimeoutException.class,
                    () -> service.execute("test.slow", objectMapper.readTree("{}")));
            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertTrue(interrupted.await(1, TimeUnit.SECONDS));
            assertTrue(invoked.get());
        }
    }

    @Test
    void rejectsInvalidResourceLimitsAtStartup() {
        ToolRegistry registry = new ToolRegistry(java.util.List.of(new WordCountTool()));
        ToolExecutionPolicy policy = new ToolExecutionPolicy("text.word-count", 8192);
        assertThrows(IllegalStateException.class,
                () -> new ToolExecutionService(registry, policy, objectMapper, 0, 1, 1));
        assertThrows(IllegalStateException.class,
                () -> new ToolExecutionService(registry, policy, objectMapper, 100, 0, 1));
        assertThrows(IllegalStateException.class,
                () -> new ToolExecutionService(registry, policy, objectMapper, 100, 1, 0));
    }
}
