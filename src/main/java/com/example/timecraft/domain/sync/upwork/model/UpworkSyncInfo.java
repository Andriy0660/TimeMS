package com.example.timecraft.domain.sync.upwork.model;

import com.example.timecraft.domain.sync.model.SyncInfo;
import com.example.timecraft.domain.sync.model.SyncStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpworkSyncInfo extends SyncInfo {
  public UpworkSyncInfo(final SyncStatus status) {
    super(status);
  }
}
