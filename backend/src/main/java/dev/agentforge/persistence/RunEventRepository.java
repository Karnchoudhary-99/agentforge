package dev.agentforge.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunEventRepository extends JpaRepository<RunEventEntity, UUID> {
    List<RunEventEntity> findByRunIdOrderByTimestampAscIdAsc(UUID runId);
    List<RunEventEntity> findByRunIdInOrderByTimestampAscIdAsc(Collection<UUID> runIds);
}
