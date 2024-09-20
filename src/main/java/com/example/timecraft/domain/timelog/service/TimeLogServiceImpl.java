package com.example.timecraft.domain.timelog.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogConfigResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFormWorklogResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import com.example.timecraft.domain.timelog.util.TimeLogUtils;
import lombok.RequiredArgsConstructor;

import static com.example.timecraft.domain.timelog.service.DurationService.formatDuration;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeLogServiceImpl implements TimeLogService {
  private final TimeLogRepository repository;
  private final TimeLogMapper mapper;
  private final Clock clock;
  private final AppProperties props;

  @Override
  public TimeLogListResponse list(final String mode, final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    if(offset < 0 || offset > 23) {
      throw new BadRequestException("Offset must be between 0 and 23");
    }
    final List<TimeLogEntity> timeLogEntityList = getAllTimeLogEntitiesInMode(mode, date, offset);

    final List<TimeLogListResponse.TimeLogDto> timeLogDtoList = timeLogEntityList.stream()
        .map(timeLogEntity -> {
          TimeLogListResponse.TimeLogDto timeLogDto = mapper.toListItem(timeLogEntity);
          timeLogDto.setTotalTime(mapTotalTime(timeLogDto.getStartTime(), timeLogDto.getEndTime()));
          return timeLogDto;
        })
        .toList();
    return new TimeLogListResponse(timeLogDtoList);
  }

  @Override
  public List<TimeLogEntity> getAllTimeLogEntitiesInMode(final String mode, final LocalDate date, final int offset) {
    final LocalTime startTime = LocalTime.of(offset, 0);
    final LocalDate[] dateRange = TimeLogUtils.calculateDateRange(mode, date);

    if ("All".equals(mode)) {
      return repository.findAll();
    } else {
      return repository.findAllInRange(dateRange[0], dateRange[1], startTime);
    }
  }

  private String mapTotalTime(final LocalTime startTime, final LocalTime endTime) {
    if (startTime == null || endTime == null) {
      return null;
    }
    Duration duration = getDurationBetweenStartAndEndTime(startTime, endTime);
    return formatDuration(duration);
  }

  private Duration getDurationBetweenStartAndEndTime(final LocalTime startTime, final LocalTime endTime) {
    Duration duration = Duration.between(startTime, endTime);
    if (endTime.isBefore(startTime)) {
      duration = duration.plusDays(1);
    }
    return duration;
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
    return mapper.toCreateResponse(timeLogEntity);
  }

  @Override
  public TimeLogCreateFormWorklogResponse createFromWorklog(final TimeLogCreateFromWorklogRequest request) {
    TimeLogEntity entity = mapper.fromCreateFromWorklogRequest(request);
    entity.setEndTime(request.getStartTime().plusSeconds(request.getTimeSpentSeconds()));
    entity = repository.save(entity);
    return mapper.toCreateFromWorklogResponse(entity);
  }

  @Override
  public void importTimeLogs(final TimeLogImportRequest request) {
    List<TimeLogImportRequest.TimeLogDateGroup> timeLogDateGroups = request.getDateGroups();
    for(TimeLogImportRequest.TimeLogDateGroup timeLogDateGroup : timeLogDateGroups) {
      List<TimeLogEntity> timeLogEntityList = repository.findAllByDateIs(timeLogDateGroup.getKey());
      for(TimeLogImportRequest.TimeLogDto timeLogDto : timeLogDateGroup.getItems()) {
        if(timeLogEntityList.stream().noneMatch(timeLogEntity -> isSameTimeLog(timeLogEntity, timeLogDto))) {
          TimeLogEntity timeLogEntity = mapper.fromMergeRequest(timeLogDto);
          if (timeLogEntity.getStartTime() != null) {
            stopOtherTimeLogs(null);
          }
          repository.save(timeLogEntity);
        }
      }
    }
  }

  @Override
  public void divide(final long timeLogId) {
    final int offset = props.getTimeConfig().getOffset();
    LocalTime startOfDay = LocalTime.of(offset, 0);
    final TimeLogEntity timeLogEntity = getRaw(timeLogId);
    TimeLogEntity secondEntity = TimeLogEntity.builder()
        .startTime(startOfDay)
        .endTime(timeLogEntity.getEndTime())
        .description(timeLogEntity.getDescription())
        .ticket(timeLogEntity.getTicket())
        .date(timeLogEntity.getStartTime().isBefore(LocalTime.of(offset, 0))
            ? timeLogEntity.getDate()
            : timeLogEntity.getDate().plusDays(1))
        .build();

    timeLogEntity.setEndTime(startOfDay);
    repository.save(timeLogEntity);
    repository.save(secondEntity);
  }

  private boolean isSameTimeLog(TimeLogEntity timeLogEntity, TimeLogImportRequest.TimeLogDto timeLogDto) {
    return Objects.equals(timeLogEntity.getTicket(), timeLogDto.getTicket())
        && Objects.equals(timeLogEntity.getStartTime(), timeLogDto.getStartTime())
        && Objects.equals(timeLogEntity.getEndTime(), timeLogDto.getEndTime())
        && Objects.equals(timeLogEntity.getDate(), timeLogDto.getDate())
        && Objects.equals(timeLogEntity.getDescription(), timeLogDto.getDescription());
  }

  private boolean isConflictedWithOthersTimeLogs(final TimeLogEntity entity, final List<TimeLogEntity> otherTimeLogs) {
    return otherTimeLogs.stream().anyMatch(timeLog ->
        !timeLog.getId().equals(entity.getId()) &&
            areIntervalsOverlapping(entity.getStartTime(), entity.getEndTime(), timeLog.getStartTime(), timeLog.getEndTime())
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
      return target.isAfter(border1) && target.isBefore(LocalTime.MAX)
          || target.isAfter(LocalTime.MIN) && target.isBefore(border2);

    } else {
      return target.isAfter(border1) && target.isBefore(border2);
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

  @Override
  public TimeLogConfigResponse getConfig() {
    return new TimeLogConfigResponse(
        props.getTimeConfig().getOffset(),
        props.getTimeConfig().getStartHourOfWorkingDay(),
        props.getTimeConfig().getEndHourOfWorkingDay()
    );
  }

  @Override
  public TimeLogHoursForWeekResponse getHoursForWeek(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();

    LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    final LocalTime startOfDay = LocalTime.of(offset, 0);
    final List<TimeLogEntity> entities = repository.findAllInRange(startOfWeek, endOfWeek.plusDays(1), startOfDay);

    return new TimeLogHoursForWeekResponse(getDayInfoList(entities, startOfWeek, endOfWeek));
  }

  private List<TimeLogHoursForWeekResponse.DayInfo> getDayInfoList(final List<TimeLogEntity> entities, final LocalDate startOfWeek,
                                                                   final LocalDate endOfWeek) {
    final int offset = props.getTimeConfig().getOffset();
    final Set<String> tickets = getTicketsForWeek(entities);

    final List<TimeLogHoursForWeekResponse.DayInfo> dayInfoList = new ArrayList<>();
    LocalDate currentDay = startOfWeek;
    while (!currentDay.isAfter(endOfWeek)) {
      List<TimeLogEntity> entitiesForDay = getAllTimeLogEntitiesInMode("Day", currentDay, offset);

      dayInfoList.add(TimeLogHoursForWeekResponse.DayInfo.builder()
          .dayName(currentDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
          .date(currentDay)
          .isConflicted(hasConflictsForDay(entitiesForDay))
          .isInProgress(hasInProgressTimeLogs(entitiesForDay))
          .ticketDurations(getTicketDurationsForDay(entitiesForDay, tickets))
          .build());

      currentDay = currentDay.plusDays(1);
    }
    return dayInfoList;
  }

  public boolean hasConflictsForDay(List<TimeLogEntity> entitiesForDay) {
    for (TimeLogEntity entity : entitiesForDay) {
      if (isConflictedWithOthersTimeLogs(entity, entitiesForDay)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasInProgressTimeLogs(List<TimeLogEntity> entitiesForDay) {
    for (TimeLogEntity entity : entitiesForDay) {
      if (entity.getStartTime() != null && entity.getEndTime() == null) {
        return true;
      }
    }
    return false;
  }


  private Set<String> getTicketsForWeek(final List<TimeLogEntity> entities) {
    final Set<String> tickets = entities.stream()
        .map(TimeLogEntity::getTicket)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (entities.stream().anyMatch(timeLogEntity -> timeLogEntity.getTicket() == null))
      tickets.add("Without Ticket");
    return tickets;
  }

  private List<TimeLogHoursForWeekResponse.TicketDuration> getTicketDurationsForDay(
      final List<TimeLogEntity> entitiesForDay, final Set<String> tickets) {

    final List<TimeLogHoursForWeekResponse.TicketDuration> ticketDurations = new ArrayList<>();
    Duration totalForDay = Duration.ZERO;

    for (String ticket : tickets) {
      Duration totalForTicket = Duration.ZERO;

      for (TimeLogEntity entity : entitiesForDay) {
        if (entity.getStartTime() != null) {
          String currentTicket = entity.getTicket() != null ? entity.getTicket() : "Without Ticket";
          if (currentTicket.equals(ticket)) {
            Duration duration = getDurationBetweenStartAndEndTime(entity.getStartTime(),
                entity.getEndTime() != null ? entity.getEndTime() : LocalTime.now(clock));
            totalForTicket = totalForTicket.plus(duration);
            totalForDay = totalForDay.plus(duration);
          }
        }
      }

      ticketDurations.add(new TimeLogHoursForWeekResponse.TicketDuration(ticket, formatDuration(totalForTicket)));
    }
    ticketDurations.add(new TimeLogHoursForWeekResponse.TicketDuration("Total", formatDuration(totalForDay)));
    return ticketDurations;
  }

  @Override
  public TimeLogHoursForMonthResponse getHoursForMonth(final LocalDate date) {
    final int offset = props.getTimeConfig().getOffset();
    final LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
    final LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());

    final List<TimeLogHoursForMonthResponse.DayInfo> dayInfoList = new ArrayList<>();
    Duration totalDuration = Duration.ZERO;
    LocalDate currentDay = startOfMonth;
    while (!currentDay.isAfter(endOfMonth)) {
      List<TimeLogEntity> entitiesForDay = getAllTimeLogEntitiesInMode("Day", currentDay, offset);

      final Duration durationForDay = getDurationForDay(entitiesForDay);
      totalDuration = totalDuration.plus(durationForDay);

      dayInfoList.add(TimeLogHoursForMonthResponse.DayInfo.builder()
          .start(LocalDateTime.of(currentDay, LocalTime.MIN))
          .title(formatDuration(durationForDay))
          .isConflicted(hasConflictsForDay(entitiesForDay))
          .isInProgress(hasInProgressTimeLogs(entitiesForDay))
          .build());

      currentDay = currentDay.plusDays(1);
    }
    return new TimeLogHoursForMonthResponse(formatDuration(totalDuration), dayInfoList);
  }

  private Duration getDurationForDay(final List<TimeLogEntity> entities) {
    Duration duration = Duration.ZERO;
    for (TimeLogEntity entity : entities) {
      if (entity.getStartTime() != null) {
        duration = duration.plus(getDurationBetweenStartAndEndTime(entity.getStartTime(),
            entity.getEndTime() != null ? entity.getEndTime() : LocalTime.now(clock)));
      }
    }
    return duration;
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
