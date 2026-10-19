package dev.agentforge.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RunController.class)
@Import({RunService.class, TaskPlanner.class})
class RunControllerTest {
    @Autowired MockMvc mvc;
    @MockBean RunPersistenceService persistence;
    private final Map<UUID, RunResponse> rows = new ConcurrentHashMap<>();

    @BeforeEach
    void stubPersistence() {
        rows.clear();
        doAnswer(call -> { RunResponse run = call.getArgument(0); rows.put(run.id(), run); return null; })
                .when(persistence).save(any(RunResponse.class));
        when(persistence.list()).thenAnswer(call -> rows.values().stream()
                .sorted(Comparator.comparing(RunResponse::createdAt).reversed()).toList());
        when(persistence.find(any(UUID.class))).thenAnswer(call -> Optional.ofNullable(rows.get(call.getArgument(0))));
    }

    @Test
    void createsRunWithPlanAndTimeline() throws Exception {
        mvc.perform(post("/api/runs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowName\":\"Research brief\",\"task\":\"Summarize key risks\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.matchesRegex("/api/runs/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.status").value("Succeeded"))
                .andExpect(jsonPath("$.plan.length()").value(3))
                .andExpect(jsonPath("$.events.length()").value(4))
                .andExpect(jsonPath("$.events[3].type").value("run.succeeded"));
    }

    @Test
    void listsAndRetrievesPersistedRunThroughStoreBoundary() throws Exception {
        var created = mvc.perform(post("/api/runs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowName\":\"Triage\",\"task\":\"Classify request\"}"))
                .andExpect(status().isCreated()).andReturn();
        String location = created.getResponse().getHeader("Location");
        mvc.perform(get("/api/runs")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workflowName").value("Triage"));
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.events.length()").value(4));
    }

    @Test
    void rejectsInvalidInputAndUnknownRun() throws Exception {
        mvc.perform(post("/api/runs").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowName\":\" ",\"task\":\"Do work\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/runs/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
