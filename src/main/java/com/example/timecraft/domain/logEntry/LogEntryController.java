package com.example.timecraft.domain.logEntry;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.logEntry.dto.LogEntryCreateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryGetResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateResponse;
import com.example.timecraft.domain.logEntry.service.LogEntryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logEntries")
public class LogEntryController {
  private final LogEntryService logEntryService;

  @GetMapping
  public LogEntryListAllResponse listAll() {
    return logEntryService.listAll();
  }

  @PostMapping
  public LogEntryCreateResponse create(@RequestBody final LogEntryCreateRequest request) {
    return logEntryService.create(request);
  }

  @GetMapping("/{logEntryId}")
  public LogEntryGetResponse get(@PathVariable final long logEntryId) {
    return logEntryService.get(logEntryId);
  }

  @PutMapping("/{logEntryId}")
  public LogEntryUpdateResponse update(@PathVariable final long logEntryId, @RequestBody final LogEntryUpdateRequest request) {
    return logEntryService.update(logEntryId, request);
  }

  @DeleteMapping("/{logEntryId}")
  public void delete(@PathVariable final long logEntryId) {
    logEntryService.delete(logEntryId);
  }

}
