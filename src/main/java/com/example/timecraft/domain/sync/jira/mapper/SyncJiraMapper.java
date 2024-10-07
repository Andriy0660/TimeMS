package com.example.timecraft.domain.sync.jira.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

@Mapper(componentModel = "spring")

public interface SyncJiraMapper {
  @Mapping(source = "comment", target = "description")
  @Mapping(target = "id", ignore = true)
  TimeLogEntity worklogToTimeLog(WorklogEntity worklogEntity);
}
