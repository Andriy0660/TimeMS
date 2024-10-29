package com.example.timecraft.domain.sync.upwork;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.timecraft.domain.sync.upwork.dto.SyncUpworkRequest;
import com.example.timecraft.domain.sync.upwork.dto.SyncUpworkStatusForDayResponse;
import com.example.timecraft.domain.sync.upwork.service.SyncUpworkService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/syncUpwork")
public class SyncUpworkController {
  private final SyncUpworkService syncUpworkService;

  @GetMapping
  public SyncUpworkStatusForDayResponse getStatusForDay(@RequestParam final LocalDate date) {
    return syncUpworkService.getSyncStatusForDay(date);
  }

  @PostMapping
  public void sync(@RequestBody final SyncUpworkRequest request) {
    syncUpworkService.sync(request);
  }
}