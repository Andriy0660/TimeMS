package com.example.timecraft.domain.sync.external_service.service;

import com.example.timecraft.domain.sync.external_service.dto.SyncIntoExternalServiceRequest;

public interface SyncExternalTimeLogService {
  void syncIntoExternalService(SyncIntoExternalServiceRequest request);
}
