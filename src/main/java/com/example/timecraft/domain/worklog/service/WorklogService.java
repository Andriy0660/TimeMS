package com.example.timecraft.domain.worklog.service;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorklogService {
  private final WorklogRepository worklogRepository;
  private final JiraWorklogService jiraWorklogService;
}
