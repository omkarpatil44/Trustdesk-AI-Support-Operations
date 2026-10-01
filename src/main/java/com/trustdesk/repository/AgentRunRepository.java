package com.trustdesk.repository;

import com.trustdesk.entity.AgentRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRunRepository extends JpaRepository<AgentRun, Long> {
    List<AgentRun> findByTicketId(String ticketId);
    List<AgentRun> findByRunType(String runType);
    List<AgentRun> findByStatus(String status);
}
