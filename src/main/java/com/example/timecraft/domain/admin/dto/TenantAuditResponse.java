package com.example.timecraft.domain.admin.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAuditResponse {
    private Long tenantId;
    private String schemaName;
    private Page<AuditLogResponse> auditLogs;
}