package com.trustdesk.repository;

import com.trustdesk.entity.ToolAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolActionRepository extends JpaRepository<ToolAction, Long> {
    Optional<ToolAction> findByIdempotencyKey(String idempotencyKey);
    List<ToolAction> findByTicketId(String ticketId);
    List<ToolAction> findByStatus(String status);
    List<ToolAction> findByCustomerId(String customerId);
    
    @Query("SELECT t FROM ToolAction t WHERE t.idempotencyKey = :idempotencyKey AND t.status IN ('requested', 'approved')")
    Optional<ToolAction> findPendingBy_IdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
}
