package com.trustdesk.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "agent_runs")
public class AgentRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "run_type", nullable = false)
    private String runType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "retrieved_doc_ids_json", length = 2000)
    private String retrievedDocIdsJson;

    @Column(name = "tool_calls_json", length = 2000)
    private String toolCallsJson;

    @Column(name = "guardrail_results_json", length = 2000)
    private String guardrailResultsJson;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime completedAt;

    public AgentRun() {}

    public AgentRun(String ticketId, String runType, String status, 
                   String retrievedDocIdsJson, String toolCallsJson,
                   String guardrailResultsJson, String modelUsed) {
        this.ticketId = ticketId;
        this.runType = runType;
        this.status = status;
        this.retrievedDocIdsJson = retrievedDocIdsJson;
        this.toolCallsJson = toolCallsJson;
        this.guardrailResultsJson = guardrailResultsJson;
        this.modelUsed = modelUsed;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getRunType() { return runType; }
    public void setRunType(String runType) { this.runType = runType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRetrievedDocIdsJson() { return retrievedDocIdsJson; }
    public void setRetrievedDocIdsJson(String retrievedDocIdsJson) { this.retrievedDocIdsJson = retrievedDocIdsJson; }

    public String getToolCallsJson() { return toolCallsJson; }
    public void setToolCallsJson(String toolCallsJson) { this.toolCallsJson = toolCallsJson; }

    public String getGuardrailResultsJson() { return guardrailResultsJson; }
    public void setGuardrailResultsJson(String guardrailResultsJson) { this.guardrailResultsJson = guardrailResultsJson; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
