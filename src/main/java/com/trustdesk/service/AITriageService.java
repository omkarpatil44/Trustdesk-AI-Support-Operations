package com.trustdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustdesk.entity.Customer;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.Ticket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class AITriageService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    /**
     * Triage a ticket using Groq LLM to classify category, priority, and escalation
     */
    public Map<String, Object> triageTicket(String ticketId, Ticket ticket, 
                                           Customer customer, List<Order> orders, 
                                           List<String> kbContext) {
        String prompt = buildPrompt(ticketId, ticket, customer, orders, kbContext);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", groqModel);
        
        List<Map<String, String>> messages = List.of(
            Map.of("role", "system", "content", "You are an AI support triage agent. Return ONLY valid JSON, no additional text."),
            Map.of("role", "user", "content", prompt)
        );
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 500);

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
                return parseTriageResponse(response.getBody());
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
     * Build the prompt for triage
     */
    private String buildPrompt(String ticketId, Ticket ticket, Customer customer, 
                              List<Order> orders, List<String> kbContext) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are an AI support triage agent. Classify the following ticket.\n\n");
        
        // Provide KB context
        if (kbContext != null && !kbContext.isEmpty()) {
            prompt.append("## Relevant Policy Documents:\n");
            for (String doc : kbContext) {
                prompt.append(doc).append("\n\n");
            }
        }
        
        // Ticket details
        prompt.append("## Ticket Details:\n");
        prompt.append("Ticket ID: ").append(ticketId).append("\n");
        prompt.append("Subject: ").append(ticket.getSubject()).append("\n");
        prompt.append("Description: ").append(ticket.getDescription()).append("\n");
        prompt.append("Status: ").append(ticket.getStatus()).append("\n");
        prompt.append("Priority: ").append(ticket.getPriority()).append("\n");
        prompt.append("Category: ").append(ticket.getCategory()).append("\n");
        prompt.append("Created At: ").append(ticket.getCreatedAt()).append("\n");
        
        // Customer context if available
        if (customer != null) {
            prompt.append("Customer ID: ").append(customer.getCustomerId()).append("\n");
            prompt.append("Customer Name: ").append(customer.getName()).append("\n");
            prompt.append("Email: ").append(customer.getEmail()).append("\n");
            prompt.append("Account Status: ").append(customer.getAccountStatus()).append("\n");
            prompt.append("Account Created: ").append(customer.getCreatedAt()).append("\n");
        }
        
        // Orders context if available
        if (orders != null && !orders.isEmpty()) {
            prompt.append("\n## Related Orders:\n");
            for (Order order : orders) {
                prompt.append("- Order ID: ").append(order.getOrderId())
                      .append(", Product: ").append(order.getProductName())
                      .append(", Category: ").append(order.getProductCategory())
                      .append(", Amount: ").append(order.getAmount())
                      .append(", Status: ").append(order.getStatus())
                      .append(", Purchased: ").append(order.getPurchaseDate()).append("\n");
            }
        }
        
        // Instructions for classification
        prompt.append("\n## Instructions:\n");
        prompt.append("Classify this ticket and provide your response in JSON format with the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"category\": \"<one of: shipping, refund, warranty, billing, account_security, general>\",\n");
        prompt.append("  \"priority\": \"<one of: low, medium, high, urgent>\",\n");
        prompt.append("  \"sentiment\": \"<one of: positive, neutral, frustrated, angry>\",\n");
        prompt.append("  \"should_escalate\": <true or false>,\n");
        prompt.append("  \"reason_summary\": \"<1-2 sentence explanation of your classification>\"\n");
        prompt.append("}\n\n");
        
        prompt.append("## Important Rules:\n");
        prompt.append("1. Review policy documents first to ensure your classification aligns with company policy.\n");
        prompt.append("2. Escalate if: security issue, account compromise, legal concern, or customer极度 frustrated.\n");
        prompt.append("3. Be conservative with 'urgent' priority - only use for security, service outage, or extreme customer distress.\n");
        prompt.append("4. If you're unsure, classify as 'general' and escalate for human review.\n");
        
        prompt.append("\n## Response (JSON only, no additional text):");
        
        return prompt.toString();
    }

    /**
     * Parse the Groq API response and extract triage result
     */
    private Map<String, Object> parseTriageResponse(String responseBody) {
        try {
            // Parse JSON response
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = choices.get(0);
                Map<String, String> messageContent = (Map<String, String>) message.get("message");
                String content = messageContent.get("content");
                
                // Parse the triage JSON from the content
                Map<String, Object> triage = objectMapper.readValue(content, Map.class);
                triage.put("modelUsed", groqModel);
                return triage;
            }
            
            return Map.of("error", "Unexpected response format", "raw", responseBody);
            
        } catch (Exception e) {
            return Map.of("error", "Failed to parse response: " + e.getMessage(), "raw", responseBody);
        }
    }
}
