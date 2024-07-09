package com.example.timecraft.domain.logEntry;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.logEntry.service.LogEntryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logEntries")
public class LogEntryController {
  private final LogEntryService logEntryService;
}
