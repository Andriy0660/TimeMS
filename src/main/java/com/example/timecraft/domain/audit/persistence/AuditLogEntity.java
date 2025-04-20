package com.example.timecraft.domain.audit.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "audit_log", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long id;

    @Column(name = "user_id")
    @ToString.Include
    private Long userId;

    @Column(name = "action")
    @ToString.Include
    private String action;

    @Column(name = "entity_type")
    @ToString.Include
    private String entityType;

    @Column(name = "entity_id")
    @ToString.Include
    private String entityId;

    @Column(name = "details")
    @ToString.Include
    private String details;

    @Column(name = "tenant_id")
    @ToString.Include
    private Long tenantId;

    @Column(name = "ip_address")
    @ToString.Include
    private String ipAddress;

    @Column(name = "timestamp")
    @ToString.Include
    private LocalDateTime timestamp;
}