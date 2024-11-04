package com.example.timecraft.domain.sync.external_timelog.service;

import com.example.timecraft.domain.sync.external_timelog.dto.SyncIntoExternalServiceRequest;

public interface SyncExternalTimeLogService {
  void syncIntoExternalService(SyncIntoExternalServiceRequest request);
}
