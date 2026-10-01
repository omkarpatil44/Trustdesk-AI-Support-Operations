package com.trustdesk.repository;

import com.trustdesk.entity.ToolCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ToolCatalogRepository extends JpaRepository<ToolCatalog, String> {
}
