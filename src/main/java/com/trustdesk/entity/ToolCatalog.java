package com.trustdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tool_catalog")
public class ToolCatalog {

    @Id
    @Column(name = "tool_name", nullable = false, unique = true)
    private String toolName;

    @Column(length = 1000)
    private String description;

    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    @Column(length = 1000)
    private String parameters;

    public ToolCatalog() {}

    public ToolCatalog(String toolName, String description, Boolean requiresApproval, String parameters) {
        this.toolName = toolName;
        this.description = description;
        this.requiresApproval = requiresApproval;
        this.parameters = parameters;
    }

    // Getters and Setters
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getRequiresApproval() { return requiresApproval; }
    public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }

    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
}
