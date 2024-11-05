package com.example.timecraft.domain.sync.external_service.model;

import com.example.timecraft.domain.sync.model.SyncInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExternalServiceSyncInfo extends SyncInfo {
  private String color;
}