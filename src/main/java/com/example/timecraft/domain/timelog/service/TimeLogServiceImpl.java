package com.example.timecraft.domain.timelog.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeLogServiceImpl implements TimeLogService {
  private final TimeLogRepository repository;
  private final TimeLogMapper mapper;
  private final Clock clock;

  @Override
  public TimeLogListResponse list(final String mode, final LocalDate date) {
    final List<TimeLogEntity> timeLogEntityList = getAllTimeLogEntitiesInMode(mode, date);
    final List<TimeLogListResponse.TimeLogDto> timeLogDtoList = timeLogEntityList.stream()
        .map(mapper::toListItem)
        .peek(timeLogDto -> timeLogDto.setTotalTime(mapTotalTime(timeLogDto.getStartTime(), timeLogDto.getEndTime())))
        .sorted(
            Comparator.comparing(
                TimeLogListResponse.TimeLogDto::getStartTime,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).thenComparing(TimeLogListResponse.TimeLogDto::getId))
        .toList();
    return new TimeLogListResponse(timeLogDtoList);
  }

  private List<TimeLogEntity> getAllTimeLogEntitiesInMode(String mode, LocalDate date) {
    switch (mode) {
      case "Day" -> {
        return repository.findAllByDateIs(date);
      }
      case "Week" -> {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return repository.findAllByDateBetween(startOfWeek, endOfWeek);
      }
      case "Month" -> {
        LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        return repository.findAllByDateBetween(startOfMonth, endOfMonth);
      }
      case "All" -> {
        return repository.findAll();
      }
      default -> throw new BadRequestException("Invalid time mode");
    }
  }

  private String mapTotalTime(LocalTime startTime, LocalTime endTime) {
    if (startTime == null || endTime == null) {
      return null;
    }
    Duration duration = Duration.between(startTime, endTime);
    if (endTime.isBefore(startTime)) {
      duration = duration.plusDays(1);
    }
    return DurationService.formatDuration(duration);
  }

  @Override
  public TimeLogCreateResponse create(final TimeLogCreateRequest request) {
    TimeLogEntity timeLogEntity = mapper.fromCreateRequest(request);
    timeLogEntity.setDate(LocalDate.now(clock));

    if(timeLogEntity.getStartTime() != null) {
      stopOtherTimeLogs(null);
    }

    timeLogEntity = repository.save(timeLogEntity);
    TimeLogCreateResponse response = mapper.toCreateResponse(timeLogEntity);
    if (isConflictedWithOthersTimeLogs(timeLogEntity, request.getStartTime(), null)) {
      response.setConflicted(true);
    }
    return response;
  }

  private boolean isConflictedWithOthersTimeLogs(final TimeLogEntity timeLogEntity, final LocalTime startTime, final LocalTime endTime) {
    final List<TimeLogEntity> timeLogEntities = repository.findAllByDateIs(timeLogEntity.getDate());
    return timeLogEntities.stream().anyMatch(timeLog ->
        !timeLog.getId().equals(timeLogEntity.getId()) &&
            areIntervalsOverlapping(startTime, endTime, timeLog.getStartTime(), timeLog.getEndTime())
    );
  }

  private boolean areIntervalsOverlapping(final LocalTime startTime1, final LocalTime endTime1,
                                          final LocalTime startTime2, final LocalTime endTime2) {
    return
        isInInterval(startTime1, endTime1, startTime2) ||
            isInInterval(startTime1, endTime1, endTime2) ||
            isInInterval(startTime2, endTime2, startTime1) ||
            isInInterval(startTime2, endTime2, endTime1);
  }

  private boolean isInInterval(final LocalTime border1, final LocalTime border2, final LocalTime target) {
    if (target == null || border1 == null || border2 == null) {
      return false;
    }
    if (border2.isBefore(border1)) {
      return  ((!target.isBefore(border1) && !target.isAfter(LocalTime.MAX))
          || (!target.isBefore(LocalTime.MIN) && !target.isAfter(border2)));

    } else {
      return !target.isBefore(border1) && !target.isAfter(border2);
    }
  }

  private void stopOtherTimeLogs(Long excludedId) {
    repository.findAllByEndTimeIsNull().forEach((timeLogEntity) -> {
      if (timeLogEntity.getId().equals(excludedId) || timeLogEntity.getStartTime() == null) {
        return;
      }
      timeLogEntity.setEndTime(LocalTime.now(clock).withSecond(0).withNano(0));
      repository.save(timeLogEntity);
    });
  }


  @Override
  public TimeLogGetResponse get(final long timeLogId) {
    final TimeLogEntity timeLogEntity = getRaw(timeLogId);
    TimeLogGetResponse response = mapper.toGetResponse(timeLogEntity);
    response.setTotalTime(mapTotalTime(timeLogEntity.getStartTime(), timeLogEntity.getEndTime()));
    return response;
  }

  private TimeLogEntity getRaw(final long timeLogId) {
    return repository.findById(timeLogId)
        .orElseThrow(() -> new NotFoundException("Log entry with such id does not exist"));
  }

  @Override
  public TimeLogUpdateResponse update(final long timeLogId, final TimeLogUpdateRequest request) {
    if (request.getEndTime() == null) {
      stopOtherTimeLogs(timeLogId);
    }

    TimeLogEntity timeLogEntity = getRaw(timeLogId);
    mapper.fromUpdateRequest(request, timeLogEntity);
    timeLogEntity = repository.save(timeLogEntity);

    TimeLogUpdateResponse response = mapper.toUpdateResponse(timeLogEntity);
    response.setTotalTime(mapTotalTime(timeLogEntity.getStartTime(), timeLogEntity.getEndTime()));
    if (isConflictedWithOthersTimeLogs(timeLogEntity, timeLogEntity.getStartTime(), timeLogEntity.getEndTime())) {
      response.setConflicted(true);
    }
    return response;
  }

  @Override
  public void delete(final long timeLogId) {
    final TimeLogEntity timeLogEntity = getRaw(timeLogId);
    repository.delete(timeLogEntity);
  }
}
