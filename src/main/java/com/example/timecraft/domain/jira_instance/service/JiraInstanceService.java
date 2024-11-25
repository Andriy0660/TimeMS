package com.example.timecraft.domain.jira_instance.service;

import com.example.timecraft.domain.jira_instance.dto.JiraInstanceSaveRequest;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceGetResponse;

public interface JiraInstanceService {
  JiraInstanceGetResponse get();
  void save(final JiraInstanceSaveRequest request);
  void delete(final Long id);
}
