package com.trustdesk.repository;

import com.trustdesk.entity.KBDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KBDocumentRepository extends JpaRepository<KBDocument, String> {
    Optional<KBDocument> findByDocId(String docId);
    List<KBDocument> findByCategory(String category);
    
    // Full-text search on title and content
    @Query("SELECT k FROM KBDocument k WHERE LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(k.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<KBDocument> searchByQuery(String query);
    
    // Search by category and query
    @Query("SELECT k FROM KBDocument k WHERE k.category = :category AND (LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(k.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<KBDocument> searchByCategoryAndQuery(String category, String query);
}
