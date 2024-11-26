package com.example.timecraft.domain.jira_instance.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira_instance.api.UserJiraInstanceService;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class RequireJiraAccountAspect {
  private final UserJiraInstanceService userJiraInstanceService;

  @Before("@within(requireJiraAccount)")
  public void beforeClass(RequireJiraAccount requireJiraAccount) {
    if(userJiraInstanceService.getJiraInstance() == null) {
      throw new BadRequestException("Add Jira account firstly");
    }
  }
}