package com.example.timecraft.domain.sync.upwork.service;

import java.time.LocalDate;

import com.example.timecraft.domain.sync.upwork.dto.SyncUpworkRequest;
import com.example.timecraft.domain.sync.upwork.dto.SyncUpworkStatusForDayResponse;

public interface SyncUpworkService {
  SyncUpworkStatusForDayResponse getSyncStatusForDay(final LocalDate date);

  void sync(final SyncUpworkRequest request);
}
