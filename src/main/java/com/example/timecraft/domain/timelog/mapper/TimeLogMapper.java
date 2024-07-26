package com.example.timecraft.domain.timelog.mapper;

import java.time.Duration;
import java.time.LocalTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.DurationService;

@Mapper(componentModel = "spring")
public interface TimeLogMapper {
  @Mapping(target = "totalTime", expression = "java(mapTotalTime(entity.getStartTime(), entity.getEndTime()))")
  TimeLogListResponse.TimeLogDto toListItem(final TimeLogEntity entity);

  default String mapTotalTime(LocalTime startTime, LocalTime endTime) {
    if (startTime == null || endTime == null) {
      return null;
    }
    Duration duration = Duration.between(startTime, endTime);
    if (endTime.isBefore(startTime)) {
      duration = duration.plusDays(1);
    }
    return DurationService.formatDuration(duration);
  }

  TimeLogEntity fromCreateRequest(final TimeLogCreateRequest request);

  TimeLogCreateResponse toCreateResponse(final TimeLogEntity entity);

  @Mapping(target = "totalTime", expression = "java(mapTotalTime(entity.getStartTime(), entity.getEndTime()))")
  TimeLogGetResponse toGetResponse(final TimeLogEntity entity);

  void fromUpdateRequest(TimeLogUpdateRequest request, @MappingTarget TimeLogEntity entity);

  @Mapping(target = "totalTime", expression = "java(mapTotalTime(entity.getStartTime(), entity.getEndTime()))")
  TimeLogUpdateResponse toUpdateResponse(TimeLogEntity entity);


}
