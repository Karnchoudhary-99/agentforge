package dev.agentforge.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApprovalServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void approvalMustBePendingAndExecutionIsSingleUse() throws Exception {
        RunService runs = new RunService(new TaskPlanner());
        RunResponse run = runs.create(new CreateRunRequest("test", "validate approval flow"));
        ToolRegistry registry = new ToolRegistry(List.of(new WordCountTool()));
        ToolExecutionPolicy policy = new ToolExecutionPolicy("text.word-count", 8192);
        try (ToolExecutionService tools = new ToolExecutionService(
                registry, policy, mapper, 500, 1, 1)) {
            ApprovalService approvals = new ApprovalService(runs, tools);
            ApprovalResponse pending = approvals.create(run.id(), "text.word-count",
                    mapper.readTree("{\"text\":\"approved action\"}"));
            assertEquals("PENDING", pending.status());
            assertThrows(ApprovalConflictException.class, () -> approvals.executeApproved(pending.id()));

            assertEquals("APPROVED", approvals.decide(pending.id(), true).status());
            assertThrows(ApprovalConflictException.class, () -> approvals.decide(pending.id(), true));
            assertEquals(2L, approvals.executeApproved(pending.id()).output().get("wordCount"));
            assertThrows(ApprovalConflictException.class, () -> approvals.executeApproved(pending.id()));
            assertEquals(3, approvals.auditEvents().size());
        }
    }

    @Test
    void rejectedApprovalCannotExecuteAndUnknownRunCannotRequestApproval() throws Exception {
        RunService runs = new RunService(new TaskPlanner());
        ToolRegistry registry = new ToolRegistry(List.of(new WordCountTool()));
        ToolExecutionPolicy policy = new ToolExecutionPolicy("text.word-count", 8192);
        try (ToolExecutionService tools = new ToolExecutionService(
                registry, policy, mapper, 500, 1, 1)) {
            ApprovalService approvals = new ApprovalService(runs, tools);
            assertThrows(ApprovalNotFoundException.class, () -> approvals.create(
                    java.util.UUID.randomUUID(), "text.word-count", mapper.readTree("{\"text\":\"no\"}")));
            RunResponse run = runs.create(new CreateRunRequest("test", "reject approval flow"));
            ApprovalResponse pending = approvals.create(run.id(), "text.word-count",
                    mapper.readTree("{\"text\":\"denied action\"}"));
            assertEquals("REJECTED", approvals.decide(pending.id(), false).status());
            assertThrows(ApprovalConflictException.class, () -> approvals.executeApproved(pending.id()));
        }
    }
}
