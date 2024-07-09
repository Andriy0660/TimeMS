package com.example.timecraft.domain.logEntry.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.domain.logEntry.dto.LogEntryCreateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryGetResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateResponse;
import com.example.timecraft.domain.logEntry.mapper.LogEntityMapper;
import com.example.timecraft.domain.logEntry.persistence.LogEntryEntity;
import com.example.timecraft.domain.logEntry.persistence.LogEntryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LogEntryServiceImpl implements LogEntryService {
  private final LogEntryRepository repository;
  private final LogEntityMapper mapper;

  @Override
  public LogEntryListAllResponse listAll() {
    final List<LogEntryEntity> logEntryEntityList = repository.findAll();
    final List<LogEntryListAllResponse.LogEntryDto> logEntryDtoList = logEntryEntityList.stream().map(mapper::toListItem).toList();
    return new LogEntryListAllResponse(logEntryDtoList);
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
