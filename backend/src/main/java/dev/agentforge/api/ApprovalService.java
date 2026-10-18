package dev.agentforge.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** In-memory, single-use approvals bound to an existing run, exact tool and exact input. */
@Service
public class ApprovalService {
    private enum Decision { PENDING, APPROVED, REJECTED, CONSUMED }
    private static final class Entry {
        final UUID id = UUID.randomUUID();
        final UUID runId;
        final String toolName;
        final JsonNode input;
        final Instant createdAt = Instant.now();
        Decision decision = Decision.PENDING;
        Instant decidedAt;

        Entry(UUID runId, String toolName, JsonNode input) {
            this.runId = runId;
            this.toolName = toolName;
            this.input = input.deepCopy();
        }

        ApprovalResponse response() {
            return new ApprovalResponse(id, runId, toolName, decision.name(), createdAt, decidedAt);
        }
    }

    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();
    private final RunService runService;
    private final ToolExecutionService executionService;
    private final List<String> audit = new java.util.concurrent.CopyOnWriteArrayList<>();

    public ApprovalService(RunService runService, ToolExecutionService executionService) {
        this.runService = runService;
        this.executionService = executionService;
    }

    public ApprovalResponse create(UUID runId, String toolName, JsonNode input) {
        if (runService.find(runId).isEmpty()) {
            throw new ApprovalNotFoundException("Run not found.");
        }
        Entry entry = new Entry(runId, toolName, input);
        entries.put(entry.id, entry);
        audit.add("approval.created id=" + entry.id + " run=" + runId + " tool=" + safeName(toolName));
        return entry.response();
    }

    public ApprovalResponse decide(UUID id, boolean approve) {
        Entry entry = require(id);
        synchronized (entry) {
            if (entry.decision != Decision.PENDING) {
                throw new ApprovalConflictException("Approval is no longer pending.");
            }
            entry.decision = approve ? Decision.APPROVED : Decision.REJECTED;
            entry.decidedAt = Instant.now();
            audit.add("approval." + (approve ? "approved" : "rejected")
                    + " id=" + entry.id + " run=" + entry.runId + " tool=" + safeName(entry.toolName));
            return entry.response();
        }
    }

    public ToolExecutionResponse executeApproved(UUID id) {
        Entry entry = require(id);
        synchronized (entry) {
            if (entry.decision != Decision.APPROVED) {
                throw new ApprovalConflictException("Approval must be approved and unused.");
            }
            // Consume before execution to prevent replay even if the tool fails.
            entry.decision = Decision.CONSUMED;
            audit.add("approval.consumed id=" + entry.id + " run=" + entry.runId
                    + " tool=" + safeName(entry.toolName));
        }
        return executionService.execute(entry.toolName, entry.input.deepCopy());
    }

    public List<String> auditEvents() {
        return List.copyOf(audit);
    }

    private Entry require(UUID id) {
        Entry entry = entries.get(id);
        if (entry == null) throw new ApprovalNotFoundException("Approval not found.");
        return entry;
    }

    private String safeName(String name) {
        return name == null ? "unknown" : name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
