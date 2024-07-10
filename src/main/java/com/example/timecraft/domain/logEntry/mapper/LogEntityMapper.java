package com.example.timecraft.domain.logEntry.mapper;

import java.time.Duration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import com.example.timecraft.domain.logEntry.dto.LogEntryCreateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryCreateResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryGetResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateRequest;
import com.example.timecraft.domain.logEntry.dto.LogEntryUpdateResponse;
import com.example.timecraft.domain.logEntry.persistence.LogEntryEntity;

@Mapper(componentModel = "spring")
public interface LogEntityMapper {
  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
  LogEntryListAllResponse.LogEntryDto toListItem(final LogEntryEntity entity);

  @Named("mapTotalTime")
  default String mapTotalTime(int timeSpentSeconds) {
    Duration duration = Duration.ofSeconds(timeSpentSeconds);
    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    minutes += duration.toSecondsPart() > 30 ? 1 : 0;

    return String.format("%dh %dm", hours, minutes);
  }

  LogEntryEntity fromCreateRequest(final LogEntryCreateRequest request);

  LogEntryCreateResponse toCreateResponse(final LogEntryEntity entity);

  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
  LogEntryGetResponse toGetResponse(final LogEntryEntity entity);

  void fromUpdateRequest(LogEntryUpdateRequest request, @MappingTarget LogEntryEntity entity);

  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
  LogEntryUpdateResponse toUpdateResponse(LogEntryEntity entity);


}
