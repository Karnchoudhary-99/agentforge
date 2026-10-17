package dev.agentforge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(RunController.class)
@Import(RunService.class)
class RunControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsMockRunAndReturnsTimeline() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workflowName":"Research brief","task":"Summarize three key risks"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.matchesRegex("/api/runs/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.workflowName").value("Research brief"))
                .andExpect(jsonPath("$.status").value("Succeeded"))
                .andExpect(jsonPath("$.summary").value(
                        "Mock execution completed safely for the submitted task. No external model or tools were invoked."))
                .andExpect(jsonPath("$.events.length()").value(2))
                .andExpect(jsonPath("$.events[0].type").value("run.created"))
                .andExpect(jsonPath("$.events[1].type").value("mock.completed"));
    }

    @Test
    void listsRunsCreatedDuringTheRequestSession() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workflowName":"Triage","task":"Classify this sample request"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workflowName").value("Triage"))
                .andExpect(jsonPath("$[0].status").value("Succeeded"));
    }

    @Test
    void retrievesRunByItsLocation() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workflowName":"Risk review","task":"List possible risks"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowName").value("Risk review"))
                .andExpect(jsonPath("$.events.length()").value(2));
    }

    @Test
    void rejectsBlankAndOversizedInput() throws Exception {
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workflowName":" ","task":"Do work"}
                                """))
                .andExpect(status().isBadRequest());

        String oversizedTask = "x".repeat(1001);
        mockMvc.perform(post("/api/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowName\":\"Test\",\"task\":\"" + oversizedTask + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownRun() throws Exception {
        mockMvc.perform(get("/api/runs/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }
}
