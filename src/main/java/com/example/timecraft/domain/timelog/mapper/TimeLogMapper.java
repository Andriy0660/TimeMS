package com.example.timecraft.domain.timelog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateFormWorklogResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

@Mapper(componentModel = "spring")
public interface TimeLogMapper {
  TimeLogListResponse.TimeLogDto toListItem(final TimeLogEntity entity);

  TimeLogEntity fromCreateRequest(final TimeLogCreateRequest request);

  TimeLogEntity fromCreateResponse(final TimeLogCreateResponse response);

  TimeLogEntity fromUpdateResponse(final TimeLogUpdateResponse response);

  TimeLogEntity fromMergeRequest(final TimeLogImportRequest.TimeLogDto timeLogDto);

  TimeLogCreateResponse toCreateResponse(final TimeLogEntity entity);

  TimeLogGetResponse toGetResponse(final TimeLogEntity entity);

  void fromUpdateRequest(final TimeLogUpdateRequest request, @MappingTarget final TimeLogEntity entity);

  TimeLogUpdateResponse toUpdateResponse(final TimeLogEntity entity);

  TimeLogEntity fromCreateFromWorklogRequest(final TimeLogCreateFromWorklogRequest request);

  TimeLogCreateFormWorklogResponse toCreateFromWorklogResponse(final TimeLogEntity entity);

  TimeLogUpdateRequest toUpdateRequest(final TimeLogEntity entity);

  @Mapping(source = "comment", target = "description")
  @Mapping(target = "id", ignore = true)
  TimeLogEntity worklogToTimeLog(final WorklogEntity worklogEntity);
}
