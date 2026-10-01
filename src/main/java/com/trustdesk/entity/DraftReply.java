package com.trustdesk.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "draft_replies")
public class DraftReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "draft_id")
    private Long draftId;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(length = 5000, nullable = false)
    private String body;

    @Column(name = "citations_json", length = 2000, nullable = false)
    private String citationsJson;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "recommended_actions_json", length = 2000)
    private String recommendedActionsJson;

    @Column(name = "refusal_reason")
    private String refusalReason;

    @Column(name = "run_id", nullable = false, unique = true)
    private String runId;

    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    public DraftReply() {}

    public DraftReply(String ticketId, String body, String citationsJson, String status,
                     String recommendedActionsJson, String refusalReason, String runId) {
        this.ticketId = ticketId;
        this.body = body;
        this.citationsJson = citationsJson;
        this.status = status;
        this.recommendedActionsJson = recommendedActionsJson;
        this.refusalReason = refusalReason;
        this.runId = runId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getDraftId() { return draftId; }
    public void setDraftId(Long draftId) { this.draftId = draftId; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCitationsJson() { return citationsJson; }
    public void setCitationsJson(String citationsJson) { this.citationsJson = citationsJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecommendedActionsJson() { return recommendedActionsJson; }
    public void setRecommendedActionsJson(String recommendedActionsJson) { this.recommendedActionsJson = recommendedActionsJson; }

    public String getRefusalReason() { return refusalReason; }
    public void setRefusalReason(String refusalReason) { this.refusalReason = refusalReason; }

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
