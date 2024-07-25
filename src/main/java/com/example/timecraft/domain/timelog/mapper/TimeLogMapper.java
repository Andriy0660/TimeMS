package com.example.timecraft.domain.timelog.mapper;

import java.time.Duration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;

@Mapper(componentModel = "spring")
public interface TimeLogMapper {
  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
  TimeLogListResponse.TimeLogDto toListItem(final TimeLogEntity entity);

  @Named("mapTotalTime")
  default String mapTotalTime(int timeSpentSeconds) {
    Duration duration = Duration.ofSeconds(timeSpentSeconds);
    return String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());
  }

  TimeLogEntity fromCreateRequest(final TimeLogCreateRequest request);

  TimeLogCreateResponse toCreateResponse(final TimeLogEntity entity);

  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
  TimeLogGetResponse toGetResponse(final TimeLogEntity entity);

  void fromUpdateRequest(TimeLogUpdateRequest request, @MappingTarget TimeLogEntity entity);

  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
  TimeLogUpdateResponse toUpdateResponse(TimeLogEntity entity);


}
