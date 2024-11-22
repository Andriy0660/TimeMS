package com.example.timecraft.domain.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateExternalServiceRequest {
  private Boolean isExternalServiceEnabled;
  private Integer externalServiceTimeCf;
  private Boolean isExternalServiceIncludeDescription;
}
