package dev.agentforge.api;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {
    private final ApprovalService approvalService;

    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping
    public ApprovalResponse request(@Valid @RequestBody ApprovalRequest request) {
        try {
            return approvalService.create(request.runId(), request.toolName(), request.input());
        } catch (ApprovalNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ApprovalResponse approve(@PathVariable UUID id) {
        return decide(id, true);
    }

    @PostMapping("/{id}/reject")
    public ApprovalResponse reject(@PathVariable UUID id) {
        return decide(id, false);
    }

    @PostMapping("/{id}/execute")
    public ToolExecutionResponse execute(@PathVariable UUID id) {
        try {
            return approvalService.executeApproved(id);
        } catch (ApprovalNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (ApprovalConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
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

    private ApprovalResponse decide(UUID id, boolean approve) {
        try {
            return approvalService.decide(id, approve);
        } catch (ApprovalNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (ApprovalConflictException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        }
    }
}
