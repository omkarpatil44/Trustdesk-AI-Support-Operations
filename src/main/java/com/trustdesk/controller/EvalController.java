package com.trustdesk.controller;

import com.trustdesk.service.EvalRunService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/evals")
@CrossOrigin(origins = "http://localhost:3000")
public class EvalController {

    private final EvalRunService evalRunService;

    public EvalController(EvalRunService evalRunService) {
        this.evalRunService = evalRunService;
    }

    /**
     * Run evaluation cases
     */
    @PostMapping("/run")
    public Map<String, Object> runEvaluations() {
        try {
            Map<String, Object> report = evalRunService.runEvaluations();
            return report;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to run evaluations: " + e.getMessage());
            return error;
        }
    }

    /**
     * Get evaluation run by ID
     */
    @GetMapping("/{evalRunId}")
    public Map<String, Object> getEvalRun(@PathVariable String evalRunId) {
        Map<String, Object> result = new HashMap<>();
        result.put("evalRunId", evalRunId);
        result.put("message", "Eval run tracking is done in-memory for this demo");
        return result;
    }
}
