package dev.agentforge.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "run_events")
public class RunEventEntity {
    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "event_timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "event_type", nullable = false, length = 80)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    protected RunEventEntity() {
    }

    public RunEventEntity(UUID id, UUID runId, Instant timestamp, String type, String message) {
        this.id = id;
        this.runId = runId;
        this.timestamp = timestamp;
        this.type = type;
        this.message = message;
    }

    public UUID getId() { return id; }
    public UUID getRunId() { return runId; }
    public Instant getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getMessage() { return message; }
}
