package com.example.timecraft.domain.external_timelog.model;

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
public class ExternalTimeLogSyncInfo extends SyncInfo {
  private String color;
}