package com.example.timecraft.domain.timelog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogMergeRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;

@Mapper(componentModel = "spring")
public interface TimeLogMapper {
  TimeLogListResponse.TimeLogDto toListItem(final TimeLogEntity entity);

  TimeLogEntity fromCreateRequest(final TimeLogCreateRequest request);
  TimeLogEntity fromMergeRequest(final TimeLogMergeRequest.TimeLogDto timeLogDto);

  TimeLogCreateResponse toCreateResponse(final TimeLogEntity entity);

  TimeLogGetResponse toGetResponse(final TimeLogEntity entity);

  void fromUpdateRequest(TimeLogUpdateRequest request, @MappingTarget TimeLogEntity entity);

  TimeLogUpdateResponse toUpdateResponse(TimeLogEntity entity);


}
