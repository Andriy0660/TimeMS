package com.example.timecraft.domain.jira_instance.api;

import org.springframework.stereotype.Service;

import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceEntity;
import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserJiraInstanceImpl implements UserJiraInstanceService {
  private final JiraInstanceRepository repository;

  @Override
  public JiraInstanceEntity getJiraInstance() {
    int size = repository.findAll().size();
    if (size == 0) {
      return null;
    }
    return repository.findAll().getFirst();
  }
}
