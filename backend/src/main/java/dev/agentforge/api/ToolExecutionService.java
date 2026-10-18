package dev.agentforge.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Enforces the independent policy boundary and bounds concurrency, payload size and execution time. */
@Service
public class ToolExecutionService implements AutoCloseable {
    private final ToolRegistry registry;
    private final ToolExecutionPolicy policy;
    private final ObjectMapper objectMapper;
    private final long timeoutMillis;
    private final ThreadPoolExecutor executor;

    public ToolExecutionService(
            ToolRegistry registry,
            ToolExecutionPolicy policy,
            ObjectMapper objectMapper,
            @Value("${agentforge.tools.timeout-ms:2000}") long timeoutMillis,
            @Value("${agentforge.tools.max-concurrent:4}") int maxConcurrent,
            @Value("${agentforge.tools.queue-capacity:16}") int queueCapacity) {
        if (timeoutMillis < 1 || maxConcurrent < 1 || queueCapacity < 1) {
            throw new IllegalStateException("Tool timeout, concurrency, and queue limits must be positive.");
        }
        this.registry = registry;
        this.policy = policy;
        this.objectMapper = objectMapper;
        this.timeoutMillis = timeoutMillis;
        this.executor = new ThreadPoolExecutor(
                maxConcurrent,
                maxConcurrent,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                runnable -> {
                    Thread thread = new Thread(runnable, "agentforge-tool-executor");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy());
    }

    public List<ToolDefinition> listAllowedTools() {
        return registry.list().stream().filter(tool -> policy.isAllowed(tool.name())).toList();
    }

    public ToolExecutionResponse execute(String toolName, JsonNode input) {
        if (!registry.contains(toolName)) {
            throw new ToolNotFoundException(toolName);
        }
        policy.assertAllowed(toolName);
        assertInputWithinLimit(input);

        Future<ToolExecutionResponse> future;
        try {
            future = executor.submit(() -> registry.execute(toolName, input));
        } catch (RejectedExecutionException exception) {
            throw new ToolCapacityExceededException();
        }

        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            future.cancel(true);
            throw new ToolExecutionTimeoutException(timeoutMillis);
        } catch (InterruptedException exception) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new ToolExecutionInterruptedException();
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ToolExecutionFailedException();
        }
    }

    private void assertInputWithinLimit(JsonNode input) {
        final int size;
        try {
            size = objectMapper.writeValueAsBytes(input).length;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Tool input must be valid JSON.", exception);
        }
        if (size > policy.maxInputBytes()) {
            throw new ToolInputTooLargeException(policy.maxInputBytes());
        }
    }

    @PreDestroy
    @Override
    public void close() {
        executor.shutdownNow();
    }
}
