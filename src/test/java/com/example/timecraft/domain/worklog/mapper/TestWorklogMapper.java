package com.example.timecraft.domain.worklog.mapper;

import org.mapstruct.Mapper;

import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

@Mapper(componentModel = "spring")
public interface TestWorklogMapper {

  WorklogEntity fromCreateResponse(final WorklogCreateFromTimeLogResponse response);
}
