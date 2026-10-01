package com.trustdesk.repository;

import com.trustdesk.entity.DraftReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DraftReplyRepository extends JpaRepository<DraftReply, Long> {
    Optional<DraftReply> findByRunId(String runId);
    List<DraftReply> findByTicketId(String ticketId);
    List<DraftReply> findByTicketIdOrderByCreatedAtDesc(String ticketId);
}
