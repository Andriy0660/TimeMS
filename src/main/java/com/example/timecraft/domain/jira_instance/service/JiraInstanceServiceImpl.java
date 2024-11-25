package com.example.timecraft.domain.jira_instance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.domain.jira_instance.dto.JiraInstanceSaveRequest;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceGetResponse;
import com.example.timecraft.domain.jira_instance.mapper.JiraInstanceMapper;
import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceEntity;
import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class JiraInstanceServiceImpl implements JiraInstanceService {
  private final JiraInstanceRepository repository;
  private final JiraInstanceMapper mapper;

  @Override
  public JiraInstanceGetResponse get() {
    int size = repository.findAll().size();
    if (size == 0) {
      return new JiraInstanceGetResponse();
    }
    return mapper.fromEntity(repository.findAll().getFirst());
  }

  @Override
  public void save(final JiraInstanceSaveRequest request) {
    final JiraInstanceEntity jiraInstanceEntity = JiraInstanceEntity.builder()
        .id(request.getId())
        .baseUrl(request.getBaseUrl())
        .email(request.getEmail())
        .token(request.getToken())
        .build();

    repository.save(jiraInstanceEntity);
  }

  @Override
  public void delete(final Long id) {
    repository.deleteById(id);
  }
}
