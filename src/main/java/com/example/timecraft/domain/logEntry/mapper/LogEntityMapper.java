package com.example.timecraft.domain.logEntry.mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.example.timecraft.domain.logEntry.dto.LogEntryListAllResponse;
import com.example.timecraft.domain.logEntry.persistence.LogEntryEntity;

@Mapper(componentModel = "spring")
public interface LogEntityMapper {
  @Mapping(target = "totalTime", source = "timeSpentSeconds", qualifiedByName = "mapTotalTime")
 LogEntryListAllResponse.LogEntryDto toListItem(final LogEntryEntity entity);

  @Named("mapTotalTime")
  default String mapTotalTime(int timeSpentSeconds) {
    Duration duration = Duration.ofSeconds(timeSpentSeconds);
    return String.format("%dh %dm", duration.toHours(), duration.toMinutesPart());
  }
}
