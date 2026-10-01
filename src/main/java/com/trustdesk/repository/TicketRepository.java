package com.trustdesk.repository;

import com.trustdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
    List<Ticket> findByCustomerId(String customerId);
    List<Ticket> findByStatus(String status);
    
    // Get recent tickets for a customer (for context)
    @Query("SELECT t FROM Ticket t WHERE t.customerId = :customerId ORDER BY t.createdAt DESC")
    List<Ticket> findRecentTicketsByCustomerId(@Param("customerId") String customerId, org.springframework.data.domain.Pageable pageable);
}
