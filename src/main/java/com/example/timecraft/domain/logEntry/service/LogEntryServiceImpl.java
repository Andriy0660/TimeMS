package com.example.timecraft.domain.logEntry.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.domain.logEntry.dto.LogEntryCreateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryGetResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateResponse;
import com.example.timecraft.domain.logEntry.persistence.LogEntryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LogEntryServiceImpl implements LogEntryService {
  private final LogEntryRepository repository;

  @Override
  public LogEntryListAllResponse listAll() {
    return null;
  }

  @Override
  public LogEntryCreateResponse create(final LogEntryCreateRequest request) {
    return null;
  }

  @Override
  public LogEntryGetResponse get(final long logEntryId) {
    return null;
  }

  @Override
  public LogEntryUpdateResponse update(final long logEntryId, final LogEntryUpdateRequest request) {
    return null;
  }

  @Override
  public void delete(final long logEntryId) {

  }
}
