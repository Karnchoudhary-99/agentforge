package dev.agentforge.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Safe example capability: counts words in a short supplied string and performs no I/O. */
@Component
public class WordCountTool implements AgentTool {
    private static final int MAX_TEXT_LENGTH = 1000;
    private static final ToolDefinition DEFINITION = new ToolDefinition(
            "text.word-count",
            "Count words and characters in a supplied text string; performs no external actions.",
            Map.of(
                    "type", "object",
                    "properties", Map.of("text", Map.of(
                            "type", "string",
                            "minLength", 1,
                            "maxLength", MAX_TEXT_LENGTH)),
                    "required", List.of("text"),
                    "additionalProperties", false));

    @Override
    public ToolDefinition definition() {
        return DEFINITION;
    }

    @Override
    public ToolExecutionResponse execute(JsonNode input) {
        if (input == null || !input.isObject()) {
            throw new IllegalArgumentException("Tool input must be a JSON object.");
        }
        if (input.size() != 1 || !input.has("text")) {
            throw new IllegalArgumentException("Expected exactly one input field: text.");
        }

        JsonNode textNode = input.get("text");
        if (!textNode.isTextual()) {
            throw new IllegalArgumentException("Input field 'text' must be a string.");
        }
        String text = textNode.textValue();
        if (text.isBlank()) {
            throw new IllegalArgumentException("Input field 'text' must not be blank.");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException(
                    "Input field 'text' must be at most " + MAX_TEXT_LENGTH + " characters.");
        }

        long wordCount = List.of(text.trim().split("\\s+")).size();
        return new ToolExecutionResponse(DEFINITION.name(), Map.of(
                "wordCount", wordCount,
                "characterCount", text.length()));
    }
}
