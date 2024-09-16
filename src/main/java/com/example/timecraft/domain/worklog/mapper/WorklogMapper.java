package com.example.timecraft.domain.worklog.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.worklog.dto.WorklogJiraDto;
import com.example.timecraft.domain.worklog.dto.WorklogListResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

@Mapper(componentModel = "spring")
public interface WorklogMapper {
  WorklogEntity toWorklogEntity(final WorklogJiraDto dto);

  WorklogListResponse.WorklogDto toListItem(final WorklogEntity entity);

}
