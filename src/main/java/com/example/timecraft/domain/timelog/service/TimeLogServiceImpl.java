package com.example.timecraft.domain.timelog.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
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
  public TimeLogListResponse list(final String mode, final LocalDate date, final int offset) {
    final List<TimeLogEntity> timeLogEntityList = getAllTimeLogEntitiesInMode(mode, date, offset);
    final List<TimeLogListResponse.TimeLogDto> timeLogDtoList = timeLogEntityList.stream()
        .map(mapper::toListItem)
        .peek(timeLogDto -> timeLogDto.setTotalTime(mapTotalTime(timeLogDto.getStartTime(), timeLogDto.getEndTime())))
        .peek(timeLogDto -> timeLogDto.setConflicted(isConflictedWithOthersTimeLogs(timeLogDto.getId(), timeLogDto.getStartTime(), timeLogDto.getEndTime(), timeLogDto.getDate())))
        .toList();
    return new TimeLogListResponse(timeLogDtoList);
  }

  private List<TimeLogEntity> getAllTimeLogEntitiesInMode(final String mode, final LocalDate date, final int offset) {
    switch (mode) {
      case "Day" -> {
        return repository.findAllInRange(date, date.plusDays(1), LocalTime.of(offset, 0));
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

  private String mapTotalTime(final LocalTime startTime, final LocalTime endTime) {
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
    if(timeLogEntity.getDate() == null) {
      timeLogEntity.setDate(LocalDate.now(clock));
    }

    if(timeLogEntity.getStartTime() != null) {
      stopOtherTimeLogs(null);
    }

    timeLogEntity = repository.save(timeLogEntity);
    TimeLogCreateResponse response = mapper.toCreateResponse(timeLogEntity);
    if (isConflictedWithOthersTimeLogs(timeLogEntity.getId(), timeLogEntity.getStartTime(), timeLogEntity.getEndTime(), timeLogEntity.getDate())) {
      response.setConflicted(true);
    }
    return response;
  }

  private boolean isConflictedWithOthersTimeLogs(final Long id, final LocalTime startTime, final LocalTime endTime,
                                                 final LocalDate date) {
    final List<TimeLogEntity> timeLogEntities = repository.findAllByDateIs(date);
    return timeLogEntities.stream().anyMatch(timeLog ->
        !timeLog.getId().equals(id) &&
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
      if(target.isBefore(border1)) {
        return false;
      }
      else {
        return  (!target.isBefore(border1) && !target.isAfter(LocalTime.MAX))
            || (!target.isBefore(LocalTime.MIN) && !target.isAfter(border2));
      }
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
        .orElseThrow(() -> new NotFoundException("Time log with such id does not exist"));
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
    if (isConflictedWithOthersTimeLogs(timeLogEntity.getId(), timeLogEntity.getStartTime(), timeLogEntity.getEndTime(), timeLogEntity.getDate())) {
      response.setConflicted(true);
    }
    return response;
  }

  @Override
  public void delete(final long timeLogId) {
    final TimeLogEntity timeLogEntity = getRaw(timeLogId);
    repository.delete(timeLogEntity);
  }

  @Override
  public void setGroupDescription(final TimeLogSetGroupDescrRequest request) {
    final List<TimeLogEntity> timeLogEntityList = repository.findAllById(request.getIds());
    timeLogEntityList.forEach(timeLogEntity -> timeLogEntity.setDescription(request.getDescription()));
  }

  @Override
  public void changeDate(final long timeLogId, final TimeLogChangeDateRequest request) {
    final TimeLogEntity timeLogEntity = getRaw(timeLogId);
    final LocalDate newDate = request.getIsNext() ? timeLogEntity.getDate().plusDays(1) : timeLogEntity.getDate().minusDays(1);
    timeLogEntity.setDate(newDate);
    repository.save(timeLogEntity);
  }
}
