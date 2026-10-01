package com.trustdesk.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tool_actions")
public class ToolAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "tool_name", nullable = false)
    private String toolName;

    @Column(length = 2000, nullable = false)
    private String payloadJson;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "requires_human_approval", nullable = false)
    private Boolean requiresHumanApproval;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "approval_required_reason")
    private String approvalRequiredReason;

    @Column(name = "execution_result")
    private String executionResult;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private String approvedBy;

    public ToolAction() {}

    public ToolAction(String ticketId, String customerId, String orderId, String toolName,
                     String payloadJson, String riskLevel, Boolean requiresHumanApproval,
                     String status, String idempotencyKey) {
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.toolName = toolName;
        this.payloadJson = payloadJson;
        this.riskLevel = riskLevel;
        this.requiresHumanApproval = requiresHumanApproval;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Boolean getRequiresHumanApproval() { return requiresHumanApproval; }
    public void setRequiresHumanApproval(Boolean requiresHumanApproval) { this.requiresHumanApproval = requiresHumanApproval; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getApprovalRequiredReason() { return approvalRequiredReason; }
    public void setApprovalRequiredReason(String approvalRequiredReason) { this.approvalRequiredReason = approvalRequiredReason; }

    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
}
