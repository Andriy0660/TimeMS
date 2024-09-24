package com.example.timecraft.domain.worklog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

@Mapper(componentModel = "spring")
public interface WorklogMapper {
  @Mapping(target = "ticket", source = "issueKey")
  WorklogEntity toWorklogEntity(final JiraWorklogDto dto);

  WorklogListResponse.WorklogDto toListItem(final WorklogEntity entity);

  WorklogCreateFromTimeLogResponse toCreateResponse(final WorklogEntity entity);

}
