package com.example.timecraft.domain.audit.persistence;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByTenantId(Long tenantId);
    Page<AuditLogEntity> findByTenantId(Long tenantId, Pageable pageable);
    Page<AuditLogEntity> findByUserId(Long userId, Pageable pageable);
}