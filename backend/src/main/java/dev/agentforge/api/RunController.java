package dev.agentforge.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    private final RunService runService;

    public RunController(RunService runService) {
        this.runService = runService;
    }

    @GetMapping
    public List<RunResponse> listRuns() {
        return runService.list();
    }

    @PostMapping
    public ResponseEntity<RunResponse> createRun(@Valid @RequestBody CreateRunRequest request) {
        RunResponse run = runService.create(request);
        return ResponseEntity.created(URI.create("/api/runs/" + run.id())).body(run);
    }

    @GetMapping("/{id}")
    public RunResponse getRun(@PathVariable UUID id) {
        return runService.find(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found"));
    }
}
