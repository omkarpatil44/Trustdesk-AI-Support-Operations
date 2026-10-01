package com.trustdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustdesk.entity.Customer;
import com.trustdesk.entity.DraftReply;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.Ticket;
import com.trustdesk.repository.DraftReplyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@Service
public class DraftGenerationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final DraftReplyRepository draftReplyRepository;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:openai/gpt-oss-120b}")
    private String groqModel;

    public DraftGenerationService(DraftReplyRepository draftReplyRepository) {
        this.draftReplyRepository = draftReplyRepository;
    }

    /**
     * Generate a draft reply for a ticket using Groq LLM
     */
    public Map<String, Object> generateDraft(String ticketId, Ticket ticket,
                                            Customer customer, List<Order> orders,
                                            List<String> kbContext) {
        String prompt = buildDraftPrompt(ticketId, ticket, customer, orders, kbContext);

        // Generate unique run ID for traceability
        String runId = "draft_" + UUID.randomUUID().toString().substring(0, 8);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", groqModel);
        
        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content", buildSystemPrompt()),
            Map.of("role", "user", "content", prompt)
        );
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 1500);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "https://api.groq.com/openai/v1/chat/completions",
                HttpMethod.POST,
                requestEntity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> draftResult = parseDraftResponse(response.getBody(), runId);
                
                // Store draft in database
                storeDraftReply(ticketId, draftResult, runId);
                
                draftResult.put("runId", runId);
                return draftResult;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Groq API call failed: " + response.getStatusCode());
                error.put("rawResponse", response.getBody());
                return error;
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to call Groq API: " + e.getMessage());
            return error;
        }
    }

    /**
     * Build the system prompt for draft generation
     */
    private String buildSystemPrompt() {
        return """
            You are an AI support agent drafting responses to customer tickets.
            
            INSTRUCTIONS:
            1. Write a professional, empathetic, and helpful response to the customer
            2. Use the provided policy documents to support your response
            3. Include citations using document IDs like [KB-REFUND-001] throughout your response
            4. If the customer's request is NOT supported by any retrieved policy, refuse politely and escalate
            5. If a tool action is recommended (refund, replacement, coupon), suggest it in the recommended_actions section
            6. Keep the tone helpful and customer-focused
            7. Do not include internal notes or instructions in the response body
            
            RETURN FORMAT:
            Return a JSON object with:
            - "body": The customer-facing response text
            - "citations": List of KB document IDs cited in the response
            - "status": "generated" or "refusal"
            - "refusal_reason": (optional) If status is "refusal", explain why
            - "recommended_actions": List of suggested tool actions with tool_name, reason, and requires_human_approval
            """;
    }

    /**
     * Build the prompt for draft generation
     */
    private String buildDraftPrompt(String ticketId, Ticket ticket, Customer customer,
                                   List<Order> orders, List<String> kbContext) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("## Ticket Details:\n");
        prompt.append("Ticket ID: ").append(ticketId).append("\n");
        prompt.append("Subject: ").append(ticket.getSubject()).append("\n");
        prompt.append("Description: ").append(ticket.getDescription()).append("\n");
        prompt.append("Created At: ").append(ticket.getCreatedAt()).append("\n");
        
        if (customer != null) {
            prompt.append("\n## Customer Context:\n");
            prompt.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
            prompt.append("Name: ").append(customer.getName()).append("\n");
            prompt.append("Email: ").append(customer.getEmail()).append("\n");
            prompt.append("Account Status: ").append(customer.getAccountStatus()).append("\n");
        }
        
        if (orders != null && !orders.isEmpty()) {
            prompt.append("\n## Related Orders:\n");
            for (Order order : orders) {
                prompt.append("- Order ID: ").append(order.getOrderId())
                      .append(", Product: ").append(order.getProductName())
                      .append(", Amount: ").append(order.getAmount())
                      .append(", Status: ").append(order.getStatus())
                      .append(", Purchased: ").append(order.getPurchaseDate()).append("\n");
            }
        }
        
        if (kbContext != null && !kbContext.isEmpty()) {
            prompt.append("\n## Policy Documents for Reference:\n");
            for (String doc : kbContext) {
                prompt.append(doc).append("\n\n");
            }
        }
        
        prompt.append("\n## Instructions:\n");
        prompt.append("Generate a customer-facing response based on the ticket and policy documents.\n");
        prompt.append("Follow the format rules and return JSON only.\n");
        
        return prompt.toString();
    }

    /**
     * Parse the Groq API response and extract draft result
     */
    private Map<String, Object> parseDraftResponse(String responseBody, String runId) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = choices.get(0);
                Map<String, String> messageContent = (Map<String, String>) message.get("message");
                String content = messageContent.get("content");
                
                // Parse the draft JSON
                Map<String, Object> draft = objectMapper.readValue(content, Map.class);
                draft.put("modelUsed", groqModel);
                return draft;
            }
            
            return Map.of("error", "Unexpected response format", "raw", responseBody);
            
        } catch (Exception e) {
            return Map.of("error", "Failed to parse response: " + e.getMessage(), "raw", responseBody);
        }
    }

    /**
     * Store the draft reply in the database
     */
    private void storeDraftReply(String ticketId, Map<String, Object> draftResult, String runId) {
        DraftReply draft = new DraftReply();
        draft.setTicketId(ticketId);
        draft.setRunId(runId);
        draft.setCreatedAt(LocalDateTime.now());
        
        // Extract body
        String body = (String) draftResult.getOrDefault("body", "");
        draft.setBody(body);
        
        // Extract citations
        List<String> citations = (List<String>) draftResult.getOrDefault("citations", List.of());
        try {
            draft.setCitationsJson(objectMapper.writeValueAsString(citations));
        } catch (Exception e) {
            draft.setCitationsJson("[]");
        }
        
        // Extract status
        String status = (String) draftResult.getOrDefault("status", "generated");
        draft.setStatus(status);
        
        // Extract refusal reason if present
        draft.setRefusalReason((String) draftResult.get("refusal_reason"));
        
        // Extract recommended actions
        List<Map<String, Object>> actions = (List<Map<String, Object>>) draftResult.get("recommended_actions");
        try {
            draft.setRecommendedActionsJson(objectMapper.writeValueAsString(actions));
        } catch (Exception e) {
            draft.setRecommendedActionsJson("[]");
        }
        
        draftReplyRepository.save(draft);
    }
}
