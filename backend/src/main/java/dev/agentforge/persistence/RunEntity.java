package dev.agentforge.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_runs")
public class RunEntity {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "workflow_name", nullable = false, length = 80)
    private String workflowName;

    @Column(nullable = false, length = 1000)
    private String task;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(nullable = false, columnDefinition = "text")
    private String summary;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "plan_json", nullable = false, columnDefinition = "text")
    private String planJson;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "failure_reason", length = 240)
    private String failureReason;

    protected RunEntity() {
    }

    public RunEntity(UUID id, String workflowName, String task, String status, String summary,
            Instant createdAt, String planJson, Long durationMs, String failureReason) {
        this.id = id;
        this.workflowName = workflowName;
        this.task = task;
        this.status = status;
        this.summary = summary;
        this.createdAt = createdAt;
        this.planJson = planJson;
        this.durationMs = durationMs;
        this.failureReason = failureReason;
    }

    public UUID getId() { return id; }
    public String getWorkflowName() { return workflowName; }
    public String getTask() { return task; }
    public String getStatus() { return status; }
    public String getSummary() { return summary; }
    public Instant getCreatedAt() { return createdAt; }
    public String getPlanJson() { return planJson; }
    public Long getDurationMs() { return durationMs; }
    public String getFailureReason() { return failureReason; }
}
