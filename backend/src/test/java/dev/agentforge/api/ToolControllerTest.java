package dev.agentforge.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;\nimport dev.agentforge.security.ApiTokenFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ToolController.class, properties = "agentforge.security.api-token=test-token")
@Import({ToolRegistry.class, WordCountTool.class, ToolExecutionPolicy.class, ToolExecutionService.class, ApiTokenFilter.class})
class ToolControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsMissingOrIncorrectApiToken() throws Exception {
        mockMvc.perform(post("/api/tools/text.word-count/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"hello"}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/tools/text.word-count/execute")
                        .header("X-AgentForge-Token", "wrong-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"hello"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listsOnlyPolicyAllowedToolsWithSchema() throws Exception {
        mockMvc.perform(get("/api/tools").header("X-AgentForge-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("text.word-count"))
                .andExpect(jsonPath("$[0].inputSchema.additionalProperties").value(false))
                .andExpect(jsonPath("$[0].inputSchema.required[0]").value("text"));
    }

    @Test
    void executesAllowlistedWordCountTool() throws Exception {
        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"AgentForge keeps tools bounded"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolName").value("text.word-count"))
                .andExpect(jsonPath("$.output.wordCount").value(4))
                .andExpect(jsonPath("$.output.characterCount").value(30));
    }

    @Test
    void rejectsUnknownToolNames() throws Exception {
        mockMvc.perform(post("/api/tools/system.shell/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsMissingExtraBlankAndOversizedInputs() throws Exception {
        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"valid","extra":true}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"   "}
                                """))
                .andExpect(status().isBadRequest());

        String oversized = "x".repeat(1001);
        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"" + oversized + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonStringTextInput() throws Exception {
        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":123}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsPayloadOverByteLimitWith413() throws Exception {
        String padding = "x".repeat(9000);
        mockMvc.perform(post("/api/tools/text.word-count/execute").header("X-AgentForge-Token", "test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"ok\",\"padding\":\"" + padding + "\"}"))
                .andExpect(status().isPayloadTooLarge());
    }
}
