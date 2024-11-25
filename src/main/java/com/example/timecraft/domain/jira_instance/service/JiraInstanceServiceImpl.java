package com.example.timecraft.domain.jira_instance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceGetResponse;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceSaveRequest;
import com.example.timecraft.domain.jira_instance.mapper.JiraInstanceMapper;
import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceEntity;
import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceRepository;
import com.example.timecraft.domain.sync.jira.api.SyncJiraAccountService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class JiraInstanceServiceImpl implements JiraInstanceService {
  private final JiraInstanceRepository repository;
  private final JiraInstanceMapper mapper;
  private final SyncJiraAccountService accountService;

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
    final String baseUrl = request.getBaseUrl();
    final String email = request.getEmail();
    final String token = request.getToken();

    if (!accountService.checkJiraAccountExists(baseUrl, email, token)) {
      throw new BadRequestException("Invalid Jira credentials");
    }
    final JiraInstanceEntity jiraInstanceEntity = JiraInstanceEntity.builder()
        .id(request.getId())
        .baseUrl(baseUrl)
        .email(email)
        .token(token)
        .build();

    repository.save(jiraInstanceEntity);
  }

  @Override
  public void delete(final Long id) {
    repository.deleteById(id);
  }
}
