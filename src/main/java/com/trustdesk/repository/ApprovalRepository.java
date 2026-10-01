package com.trustdesk.repository;

import com.trustdesk.entity.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {
    Optional<Approval> findByToolActionId(Long toolActionId);
    List<Approval> findByToolActionIdOrderByCreatedAtDesc(Long toolActionId);
    List<Approval> findByReviewerId(String reviewerId);
}
