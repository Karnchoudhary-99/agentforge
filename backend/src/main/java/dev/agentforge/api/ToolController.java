package dev.agentforge.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tools")
public class ToolController {
    private final ToolExecutionService executionService;

    public ToolController(ToolExecutionService executionService) {
        this.executionService = executionService;
    }

    @GetMapping
    public List<ToolDefinition> listTools() {
        return executionService.listAllowedTools();
    }

    @PostMapping("/{toolName}/execute")
    public ToolExecutionResponse executeTool(
            @PathVariable String toolName, @RequestBody JsonNode input) {
        try {
            return executionService.execute(toolName, input);
        } catch (ToolNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (ToolPolicyDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        } catch (ToolInputTooLargeException exception) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, exception.getMessage());
        } catch (ToolExecutionTimeoutException exception) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, exception.getMessage());
        } catch (ToolCapacityExceededException | ToolExecutionInterruptedException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        } catch (ToolExecutionFailedException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Tool execution failed.");
        }
    }
}
