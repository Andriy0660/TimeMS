package com.example.timecraft.domain.manager.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private List<String> roles;
    private List<TenantInfoResponse> tenants;
}