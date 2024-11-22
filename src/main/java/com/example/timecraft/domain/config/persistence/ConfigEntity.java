package com.example.timecraft.domain.config.persistence;

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
@Table(name = "config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class ConfigEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @ToString.Include
  private Long id;

  @Column(name = "day_offset_hour")
  @ToString.Include
  private Integer dayOffsetHour;

  @Column(name = "working_day_start_hour")
  @ToString.Include
  private Integer workingDayStartHour;

  @Column(name = "working_day_end_hour")
  @ToString.Include
  private Integer workingDayEndHour;

  @Column(name = "is_jira_enabled")
  @ToString.Include
  private Boolean isJiraEnabled;

  @Column(name = "is_external_service_enabled")
  @ToString.Include
  private Boolean isExternalServiceEnabled;

  @Column(name = "external_service_time_cf")
  @ToString.Include
  private Integer externalServiceTimeCf;

  @Column(name = "is_external_service_include_description")
  @ToString.Include
  private Boolean isExternalServiceIncludeDescription;
}
