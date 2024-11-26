package com.example.timecraft.domain.jira_instance.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.jira_instance.dto.JiraInstanceGetResponse;
import com.example.timecraft.domain.jira_instance.persistence.JiraInstanceEntity;

@Mapper(componentModel = "spring")
public interface JiraInstanceMapper {
  JiraInstanceGetResponse fromEntity(final JiraInstanceEntity entity);
}
