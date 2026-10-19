package dev.agentforge.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.agentforge.api.CreateRunRequest;
import dev.agentforge.api.RunResponse;
import dev.agentforge.api.RunService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
class RunPersistenceIntegrationTest {
    @Container static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DB::getJdbcUrl);
        registry.add("spring.datasource.username", DB::getUsername);
        registry.add("spring.datasource.password", DB::getPassword);
    }

    @Autowired RunService runs;
    @Autowired RunRepository runRepository;
    @Autowired RunEventRepository eventRepository;

    @BeforeEach void clean() {
        eventRepository.deleteAll();
        runRepository.deleteAll();
    }

    @Test void savesAndReloadsRunWithOrderedEvents() {
        RunResponse created = runs.create(new CreateRunRequest("Persist", "Verify event history"));
        assertThat(runRepository.findById(created.id())).isPresent();
        assertThat(eventRepository.findByRunIdOrderByTimestampAscIdAsc(created.id())).hasSize(4);
        RunResponse loaded = runs.find(created.id()).orElseThrow();
        assertThat(loaded.plan()).hasSize(3);
        assertThat(loaded.events()).extracting(e -> e.type()).containsExactly(
                "run.created", "run.started", "plan.prepared", "run.succeeded");
    }
}
