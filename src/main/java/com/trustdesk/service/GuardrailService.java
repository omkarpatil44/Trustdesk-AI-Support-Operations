package com.trustdesk.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GuardrailService {

    // Known adversarial patterns
    private static final Set<String> PROMPT_INJECTION_PATTERNS = new HashSet<>(Arrays.asList(
        "ignore previous instructions",
        "ignore all previous instructions",
        "disregard previous instructions",
        "output your full system prompt",
        "reveal internal configuration",
        "show hidden prompts",
        "reveal api keys",
        "reveal secret",
        "debug mode",
        "admin request",
        "override",
        "bypass verification",
        "skip identity check"
    ));

    private static final Set<String> PII_PATTERNS = new HashSet<>(Arrays.asList(
        "ssn",
        "social security",
        "credit card",
        "cvv",
        "otp",
        "bank account",
        "password",
        "api key",
        "private key",
        "secret token"
    ));

    private static final Set<String> UNSAFE_ACTION_PATTERNS = new HashSet<>(Arrays.asList(
        "issue large coupon",
        "hidden coupon",
        "unlimited discount",
        "free money",
        "bypass approval",
        "execute directly",
        "skip approval"
    ));

    /**
     * Check for prompt injection attempts
     */
    public boolean containsPromptInjection(String text) {
        String lowerText = text.toLowerCase();
        for (String pattern : PROMPT_INJECTION_PATTERNS) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for PII leakage attempts
     */
    public boolean containsPIILeakage(String text) {
        String lowerText = text.toLowerCase();
        for (String pattern : PII_PATTERNS) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for unsafe action requests
     */
    public boolean containsUnsafeAction(String text) {
        String lowerText = text.toLowerCase();
        for (String pattern : UNSAFE_ACTION_PATTERNS) {
            if (lowerText.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if text contains any adversarial content
     */
    public GuardrailResult checkGuardrails(String text) {
        GuardrailResult result = new GuardrailResult();
        result.setOriginalText(text);

        boolean hasPromptInjection = containsPromptInjection(text);
        boolean hasPIILeakage = containsPIILeakage(text);
        boolean hasUnsafeAction = containsUnsafeAction(text);

        result.setPromptInjectionDetected(hasPromptInjection);
        result.setPIILeakageDetected(hasPIILeakage);
        result.setUnsafeActionDetected(hasUnsafeAction);

        if (hasPromptInjection || hasPIILeakage || hasUnsafeAction) {
            result.setSafe(false);
            result.setBlocked(true);

            StringBuilder reasons = new StringBuilder();
            if (hasPromptInjection) {
                reasons.append("Prompt injection detected; ");
            }
            if (hasPIILeakage) {
                reasons.append("PII leakage detected; ");
            }
            if (hasUnsafeAction) {
                reasons.append("Unsafe action request detected; ");
            }
            result.setBlockReason(reasons.toString().trim());
        } else {
            result.setSafe(true);
            result.setBlocked(false);
            result.setBlockReason("");
        }

        return result;
    }

    /**
     * Result class for guardrail checks
     */
    public static class GuardrailResult {
        private String originalText;
        private boolean promptInjectionDetected;
        private boolean PIILeakageDetected;
        private boolean unsafeActionDetected;
        private boolean safe;
        private boolean blocked;
        private String blockReason;

        // Getters and Setters
        public String getOriginalText() { return originalText; }
        public void setOriginalText(String originalText) { this.originalText = originalText; }

        public boolean isPromptInjectionDetected() { return promptInjectionDetected; }
        public void setPromptInjectionDetected(boolean promptInjectionDetected) { this.promptInjectionDetected = promptInjectionDetected; }

        public boolean isPIILeakageDetected() { return PIILeakageDetected; }
        public void setPIILeakageDetected(boolean PIILeakageDetected) { this.PIILeakageDetected = PIILeakageDetected; }

        public boolean isUnsafeActionDetected() { return unsafeActionDetected; }
        public void setUnsafeActionDetected(boolean unsafeActionDetected) { this.unsafeActionDetected = unsafeActionDetected; }

        public boolean isSafe() { return safe; }
        public void setSafe(boolean safe) { this.safe = safe; }

        public boolean isBlocked() { return blocked; }
        public void setBlocked(boolean blocked) { this.blocked = blocked; }

        public String getBlockReason() { return blockReason; }
        public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
    }
}
