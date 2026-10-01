package com.trustdesk.controller;

import com.trustdesk.entity.ToolAction;
import com.trustdesk.repository.CustomerRepository;
import com.trustdesk.repository.OrderRepository;
import com.trustdesk.repository.TicketRepository;
import com.trustdesk.service.RetrievalService;
import com.trustdesk.service.ToolActionService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tool-actions")
@CrossOrigin(origins = "http://localhost:3000")
public class ToolActionController {

    private final ToolActionService toolActionService;
    private final RetrievalService retrievalService;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    public ToolActionController(ToolActionService toolActionService,
                               RetrievalService retrievalService,
                               CustomerRepository customerRepository,
                               OrderRepository orderRepository,
                               TicketRepository ticketRepository) {
        this.toolActionService = toolActionService;
        this.retrievalService = retrievalService;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Request a tool action (AI recommends; human must approve)
     */
    @PostMapping
    public Map<String, Object> requestToolAction(@RequestBody Map<String, Object> request) {
        String ticketId = (String) request.get("ticketId");
        String toolName = (String) request.get("toolName");
        String payloadJson = (String) request.get("payload");
        String idempotencyKey = (String) request.get("idempotencyKey");
        
        if (ticketId == null || toolName == null || payloadJson == null || idempotencyKey == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Missing required fields: ticketId, toolName, payload, idempotencyKey");
            return error;
        }

        // Get ticket to find customer and order
        var ticket = retrievalService.getTicketById(ticketId);
        if (ticket == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Ticket not found");
            return error;
        }

        String customerId = ticket.getCustomerId();
        String orderId = ticket.getOrderId();

        // Check for existing action with same idempotency key
        ToolAction existingAction = toolActionService.findPendingBy_IdempotencyKey(idempotencyKey);
        if (existingAction != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("actionId", existingAction.getActionId());
            result.put("idempotencyKey", idempotencyKey);
            result.put("status", existingAction.getStatus());
            result.put("message", "Idempotency key already used - returning existing action");
            return result;
        }

        // Request the action
        ToolAction action = toolActionService.requestAction(
            ticketId, customerId, orderId, toolName, payloadJson,
            "high", // Risk level - adjust based on tool
            true,   // Requires human approval
            idempotencyKey
        );

        Map<String, Object> result = new HashMap<>();
        result.put("actionId", action.getActionId());
        result.put("ticketId", ticketId);
        result.put("toolName", toolName);
        result.put("status", action.getStatus());
        result.put("requiresHumanApproval", action.getRequiresHumanApproval());
        result.put("idempotencyKey", action.getIdempotencyKey());
        result.put("message", "Action requested. Waiting for human approval.");

        return result;
    }

    /**
     * Approve or reject a tool action
     */
    @PostMapping("/{actionId}/approve")
    public Map<String, Object> approveToolAction(@PathVariable Long actionId,
                                                 @RequestBody Map<String, Object> request) {
        String decision = (String) request.get("decision");
        String reviewerId = (String) request.getOrDefault("reviewerId", "admin");
        String reason = (String) request.getOrDefault("reason", "");

        if (decision == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Missing required field: decision (approved/rejected)");
            return error;
        }

        try {
            ToolAction action = toolActionService.approveAction(actionId, reviewerId, decision, reason);
            
            Map<String, Object> result = new HashMap<>();
            result.put("actionId", action.getActionId());
            result.put("status", action.getStatus());
            result.put("approvedBy", action.getApprovedBy());
            result.put("approvedAt", action.getApprovedAt());
            result.put("message", decision.equals("approved") ? "Action approved" : "Action rejected");

            return result;
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    /**
     * Execute an approved tool action
     */
    @PostMapping("/{actionId}/execute")
    public Map<String, Object> executeToolAction(@PathVariable Long actionId) {
        try {
            // Simulate tool execution (in real app, this would call external service)
            String result = String.format("Tool action executed successfully at %s", java.time.LocalDateTime.now());
            
            ToolAction action = toolActionService.executeAction(actionId, result);
            
            Map<String, Object> resultObj = new HashMap<>();
            resultObj.put("actionId", action.getActionId());
            resultObj.put("status", action.getStatus());
            resultObj.put("executionResult", action.getExecutionResult());

            return resultObj;
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    /**
     * Get action details by ID
     */
    @GetMapping("/{actionId}")
    public Map<String, Object> getToolAction(@PathVariable Long actionId) {
        ToolAction action = toolActionService.getActionById(actionId);
        if (action == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Tool action not found");
            return error;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("actionId", action.getActionId());
        result.put("ticketId", action.getTicketId());
        result.put("customerId", action.getCustomerId());
        result.put("orderId", action.getOrderId());
        result.put("toolName", action.getToolName());
        result.put("payload", action.getPayloadJson());
        result.put("riskLevel", action.getRiskLevel());
        result.put("requiresHumanApproval", action.getRequiresHumanApproval());
        result.put("status", action.getStatus());
        result.put("idempotencyKey", action.getIdempotencyKey());
        result.put("approvalRequiredReason", action.getApprovalRequiredReason());
        result.put("executionResult", action.getExecutionResult());
        result.put("createdAt", action.getCreatedAt());
        result.put("approvedAt", action.getApprovedAt());
        result.put("approvedBy", action.getApprovedBy());

        return result;
    }

    /**
     * Get actions by ticket ID
     */
    @GetMapping("/ticket/{ticketId}")
    public Map<String, Object> getToolActionsByTicket(@PathVariable String ticketId) {
        var actions = toolActionService.getActionsByTicketId(ticketId);
        Map<String, Object> result = new HashMap<>();
        result.put("ticketId", ticketId);
        result.put("actions", actions);
        result.put("total", actions.size());

        return result;
    }

    /**
     * Get pending actions (needs approval)
     */
    @GetMapping("/pending")
    public Map<String, Object> getPendingActions() {
        var actions = toolActionService.getActionsByStatus("requested");
        Map<String, Object> result = new HashMap<>();
        result.put("pendingActions", actions);
        result.put("total", actions.size());

        return result;
    }
}
