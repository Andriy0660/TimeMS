package com.example.timecraft.domain.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfoResponse {
    private Long id;
    private String schemaName;
    private Long userId;
    private String userEmail;
}