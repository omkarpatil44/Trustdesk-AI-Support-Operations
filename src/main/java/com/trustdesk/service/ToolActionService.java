package com.trustdesk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustdesk.entity.Approval;
import com.trustdesk.entity.ToolAction;
import com.trustdesk.repository.ApprovalRepository;
import com.trustdesk.repository.ToolActionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ToolActionService {

    private final ToolActionRepository toolActionRepository;
    private final ApprovalRepository approvalRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ToolActionService(ToolActionRepository toolActionRepository,
                            ApprovalRepository approvalRepository) {
        this.toolActionRepository = toolActionRepository;
        this.approvalRepository = approvalRepository;
    }

    /**
     * Request a tool action (create new action)
     */
    public ToolAction requestAction(String ticketId, String customerId, String orderId,
                                   String toolName, String payloadJson, String riskLevel,
                                   Boolean requiresHumanApproval, String idempotencyKey) {
        // Check for existing action with same idempotency key
        Optional<ToolAction> existingAction = toolActionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingAction.isPresent()) {
            return existingAction.get();
        }

        ToolAction action = new ToolAction(
            ticketId, customerId, orderId, toolName,
            payloadJson, riskLevel, requiresHumanApproval,
            "requested", idempotencyKey
        );

        return toolActionRepository.save(action);
    }

    /**
     * Approve a tool action
     */
    public ToolAction approveAction(Long actionId, String reviewerId, String decision, String reason) {
        Optional<ToolAction> actionOpt = toolActionRepository.findById(actionId);
        if (actionOpt.isEmpty()) {
            throw new RuntimeException("Tool action not found");
        }

        ToolAction action = actionOpt.get();
        if (!action.getStatus().equals("requested")) {
            throw new RuntimeException("Action is not in requested state");
        }

        if (!decision.equals("approved") && !decision.equals("rejected")) {
            throw new RuntimeException("Invalid decision. Must be 'approved' or 'rejected'");
        }

        // Update action status
        action.setStatus(decision.equals("approved") ? "approved" : "rejected");
        action.setApprovedAt(LocalDateTime.now());
        action.setApprovedBy(reviewerId);
        action.setApprovalRequiredReason(reason);
        
        ToolAction updated = toolActionRepository.save(action);

        // Store approval record
        Approval approval = new Approval(actionId, reviewerId, decision, reason);
        approvalRepository.save(approval);

        return updated;
    }

    /**
     * Execute a tool action
     */
    public ToolAction executeAction(Long actionId, String result) {
        Optional<ToolAction> actionOpt = toolActionRepository.findById(actionId);
        if (actionOpt.isEmpty()) {
            throw new RuntimeException("Tool action not found");
        }

        ToolAction action = actionOpt.get();
        if (!action.getStatus().equals("approved")) {
            throw new RuntimeException("Action must be approved before execution");
        }

        action.setStatus("executed");
        action.setExecutionResult(result);
        
        return toolActionRepository.save(action);
    }

    /**
     * Get action by ID
     */
    public ToolAction getActionById(Long actionId) {
        return toolActionRepository.findById(actionId).orElse(null);
    }

    /**
     * Get actions by ticket ID
     */
    public List<ToolAction> getActionsByTicketId(String ticketId) {
        return toolActionRepository.findByTicketId(ticketId);
    }

    /**
     * Get actions by status
     */
    public List<ToolAction> getActionsByStatus(String status) {
        return toolActionRepository.findByStatus(status);
    }

    /**
     * Check for pending action with idempotency key
     */
    public ToolAction findPendingBy_IdempotencyKey(String idempotencyKey) {
        return toolActionRepository.findPendingBy_IdempotencyKey(idempotencyKey).orElse(null);
    }
}
