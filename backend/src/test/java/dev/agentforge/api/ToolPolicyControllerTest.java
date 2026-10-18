package dev.agentforge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ToolController.class, properties = "agentforge.security.api-token=test-token")
@Import({ToolRegistry.class, WordCountTool.class, ToolExecutionPolicy.class, ToolExecutionService.class})
@TestPropertySource(properties = {"agentforge.tools.allowed=another.registered.tool", "agentforge.security.api-token=test-token"})
class ToolPolicyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void policyDeniedRegisteredToolIsNotListedAndReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/tools").header("X-AgentForge-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .json("[]"));

        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"must not execute"}
                                """))
                .andExpect(status().isForbidden());
    }
}
