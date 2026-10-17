package dev.agentforge.api;

import java.util.List;
import org.springframework.stereotype.Component;

/** Produces a fixed, bounded plan; user text is data, never executable instructions. */
@Component
public class TaskPlanner {
    public List<PlanStep> createPlan(String workflowName, String task) {
        return List.of(
                new PlanStep(1, "Validate request",
                        "Confirm required fields and size limits before execution.", "COMPLETED"),
                new PlanStep(2, "Prepare bounded plan",
                        "Prepare a fixed mock plan for workflow: " + workflowName.trim() + ".", "COMPLETED"),
                new PlanStep(3, "Complete mock execution",
                        "Record a deterministic result without model calls, tools, or network access.", "COMPLETED"));
    }
}
