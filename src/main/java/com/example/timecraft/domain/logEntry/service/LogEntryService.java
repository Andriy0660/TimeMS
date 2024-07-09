package com.example.timecraft.domain.logEntry.service;

import com.example.timecraft.domain.logEntry.dto.LogEntryCreateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryGetResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateResponse;

public interface LogEntryService {
  LogEntryListAllResponse listAll();

  LogEntryCreateResponse create(final LogEntryCreateRequest request);

  LogEntryGetResponse get(final long logEntryId);

  LogEntryUpdateResponse update(final long logEntryId, final LogEntryUpdateRequest request);

  void delete(final long logEntryId);
}
