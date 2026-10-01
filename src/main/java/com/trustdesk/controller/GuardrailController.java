package com.trustdesk.controller;

import com.trustdesk.service.GuardrailService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/guardrail")
@CrossOrigin(origins = "http://localhost:3000")
public class GuardrailController {

    private final GuardrailService guardrailService;

    public GuardrailController(GuardrailService guardrailService) {
        this.guardrailService = guardrailService;
    }

    /**
     * Check guardrails on a text input
     */
    @PostMapping("/check")
    public Map<String, Object> checkGuardrails(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        
        if (text == null || text.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Missing required field: text");
            return error;
        }

        GuardrailService.GuardrailResult result = guardrailService.checkGuardrails(text);

        Map<String, Object> response = new HashMap<>();
        response.put("originalText", result.getOriginalText());
        response.put("safe", result.isSafe());
        response.put("blocked", result.isBlocked());
        response.put("blockReason", result.getBlockReason());
        response.put("promptInjectionDetected", result.isPromptInjectionDetected());
        response.put("piiLeakageDetected", result.isPIILeakageDetected());
        response.put("unsafeActionDetected", result.isUnsafeActionDetected());

        return response;
    }

    /**
     * Test prompt injection detection
     */
    @PostMapping("/test/prompt-injection")
    public Map<String, Object> testPromptInjection(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        boolean isPromptInjection = guardrailService.containsPromptInjection(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("promptInjectionDetected", isPromptInjection);

        return response;
    }

    /**
     * Test PII leakage detection
     */
    @PostMapping("/test/pii")
    public Map<String, Object> testPIILeakage(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        boolean isPIILeakage = guardrailService.containsPIILeakage(text);

        Map<String, Object> response = new HashMap<>();
        response.put("text", text);
        response.put("piiLeakageDetected", isPIILeakage);

        return response;
    }
}
