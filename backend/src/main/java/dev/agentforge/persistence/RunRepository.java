package dev.agentforge.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepository extends JpaRepository<RunEntity, UUID> {
    List<RunEntity> findAllByOrderByCreatedAtDesc();
}
