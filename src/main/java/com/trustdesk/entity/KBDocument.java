package com.trustdesk.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "kb_documents")
public class KBDocument {

    @Id
    @Column(name = "doc_id", nullable = false, unique = true)
    private String docId;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000, nullable = false)
    private String content;

    private String category;

    @Column(length = 1000)
    private String tags;

    public KBDocument() {}

    public KBDocument(String docId, String title, String content, String category, String tags) {
        this.docId = docId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.tags = tags;
    }

    // Getters and Setters
    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}
