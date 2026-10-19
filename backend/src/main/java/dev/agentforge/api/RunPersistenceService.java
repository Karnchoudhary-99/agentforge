package dev.agentforge.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.agentforge.persistence.RunEntity;
import dev.agentforge.persistence.RunEventEntity;
import dev.agentforge.persistence.RunEventRepository;
import dev.agentforge.persistence.RunRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RunPersistenceService {
    private static final TypeReference<List<PlanStep>> PLAN_TYPE = new TypeReference<>() {};
    private final RunRepository runs;
    private final RunEventRepository events;
    private final ObjectMapper mapper;

    public RunPersistenceService(RunRepository runs, RunEventRepository events, ObjectMapper mapper) {
        this.runs = runs;
        this.events = events;
        this.mapper = mapper;
    }

    @Transactional
    public void save(RunResponse run) {
        String plan;
        try {
            plan = mapper.writeValueAsString(run.plan());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize run plan.", e);
        }
        runs.saveAndFlush(new RunEntity(run.id(), run.workflowName(), run.task(), run.status(),
                run.summary(), run.createdAt(), plan));
        events.saveAll(run.events().stream().map(e -> new RunEventEntity(
                UUID.randomUUID(), run.id(), e.timestamp(), e.type(), e.message())).toList());
    }

    @Transactional(readOnly = true)
    public List<RunResponse> list() {
        return runs.findAllByOrderByCreatedAtDesc().stream()
                .map(r -> toResponse(r, events.findByRunIdOrderByTimestampAscIdAsc(r.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<RunResponse> find(UUID id) {
        return runs.findById(id).map(r -> toResponse(r, events.findByRunIdOrderByTimestampAscIdAsc(id)));
    }

    private RunResponse toResponse(RunEntity r, List<RunEventEntity> rows) {
        try {
            List<PlanStep> plan = mapper.readValue(r.getPlanJson(), PLAN_TYPE);
            List<RunEvent> timeline = rows.stream()
                    .map(e -> new RunEvent(e.getTimestamp(), e.getType(), e.getMessage())).toList();
            return new RunResponse(r.getId(), r.getWorkflowName(), r.getTask(), r.getStatus(),
                    r.getSummary(), r.getCreatedAt(), plan, timeline);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to read persisted run plan.", e);
        }
    }
}
