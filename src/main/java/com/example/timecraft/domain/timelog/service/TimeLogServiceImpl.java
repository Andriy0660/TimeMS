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

import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogGetResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForMonthResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogHoursForWeekResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogListResponse;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateResponse;
import com.example.timecraft.domain.timelog.mapper.TimeLogMapper;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import lombok.RequiredArgsConstructor;

import static com.example.timecraft.domain.timelog.service.DurationService.formatDuration;

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
        .toList();
    return new TimeLogListResponse(timeLogDtoList);
  }

  private List<TimeLogEntity> getAllTimeLogEntitiesInMode(final String mode, final LocalDate date, final int offset) {
    final LocalTime startTime = LocalTime.of(offset, 0);
    switch (mode) {
      case "Day" -> {
        return repository.findAllInRange(date, date.plusDays(1), startTime);
      }
      case "Week" -> {
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return repository.findAllInRange(startOfWeek, endOfWeek.plusDays(1), startTime);
      }
      case "Month" -> {
        LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        return repository.findAllInRange(startOfMonth, endOfMonth.plusDays(1), startTime);
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

  @Override
  public TimeLogHoursForWeekResponse getHoursForWeek(final LocalDate date, final int offset) {
    LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    final LocalTime startOfDay = LocalTime.of(offset, 0);
    List<TimeLogEntity> entities = repository.findAllInRange(startOfWeek, endOfWeek.plusDays(1), startOfDay);

    List<TimeLogHoursForWeekResponse.DayInfo> dayInfoList = getDayInfoList(entities, startOfWeek, endOfWeek, startOfDay);

    return new TimeLogHoursForWeekResponse(dayInfoList);
  }

  private List<TimeLogHoursForWeekResponse.DayInfo> getDayInfoList(final List<TimeLogEntity> entities, final LocalDate startOfWeek,
                                                                   final LocalDate endOfWeek, final LocalTime startOfDay) {
    Set<String> tickets = getTicketsForWeek(entities);

    List<TimeLogHoursForWeekResponse.DayInfo> dayInfoList = new ArrayList<>();
    LocalDate currentDay = startOfWeek;
    while (!currentDay.isAfter(endOfWeek)) {
      List<TimeLogHoursForWeekResponse.TicketDuration> ticketDurations = getTicketDurationsForDay(entities, startOfDay, tickets, currentDay);
      dayInfoList.add(TimeLogHoursForWeekResponse.DayInfo.builder()
          .dayName(currentDay.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH))
          .date(currentDay)
          .ticketDurations(ticketDurations)
          .build());

      currentDay = currentDay.plusDays(1);
    }
    return dayInfoList;
  }

  private Set<String> getTicketsForWeek(final List<TimeLogEntity> entities) {
    Set<String> tickets = entities.stream()
        .map(TimeLogEntity::getTicket)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    tickets.add("Without Ticket");
    return tickets;
  }

  private List<TimeLogHoursForWeekResponse.TicketDuration> getTicketDurationsForDay(
      final List<TimeLogEntity> entities,
      final LocalTime startOfDay,
      final Set<String> tickets,
      final LocalDate currentDay) {
    List<TimeLogHoursForWeekResponse.TicketDuration> ticketDurations = new ArrayList<>();
    Duration totalForDay = Duration.ZERO;

    for (String ticket : tickets) {
      Duration totalForTicket = Duration.ZERO;

      for (TimeLogEntity entity : entities) {
        if (entity.getStartTime() == null || entity.getEndTime() == null) {
          continue;
        }
        if ((entity.getDate().isEqual(currentDay) && !entity.getStartTime().isBefore(startOfDay)) ||
            (entity.getDate().minusDays(1).equals(currentDay) && entity.getStartTime().isBefore(startOfDay))) {

          String currentTicket = entity.getTicket() != null ? entity.getTicket() : "Without Ticket";
          if (currentTicket.equals(ticket)) {
            Duration duration = getDurationBetweenStartAndEndTime(entity.getStartTime(), entity.getEndTime());
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
  public TimeLogHoursForMonthResponse getHoursForMonth(final LocalDate date, final int offset) {
    LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
    final LocalTime startOfDay = LocalTime.of(offset, 0);

    List<TimeLogEntity> entities = repository.findAllInRange(startOfMonth, endOfMonth.plusDays(1), startOfDay);
    List<TimeLogHoursForMonthResponse.DayInfo> dayInfoList = new ArrayList<>();
    LocalDate currentDay = startOfMonth;
    while (!currentDay.isAfter(endOfMonth)) {
      dayInfoList.add(TimeLogHoursForMonthResponse.DayInfo.builder()
          .start(LocalDateTime.of(currentDay, LocalTime.MIN))
          .title(getDurationForDay(entities, currentDay, startOfDay))
          .build());

      currentDay = currentDay.plusDays(1);
    }
    return new TimeLogHoursForMonthResponse(dayInfoList);
  }

  private String getDurationForDay(final List<TimeLogEntity> entities, final LocalDate date, final LocalTime startOfDay) {
    Duration duration = Duration.ZERO;
    for (TimeLogEntity entity : entities) {
      if (entity.getStartTime() == null || entity.getEndTime() == null) {
        continue;
      }
      if ((entity.getDate().isEqual(date) && !entity.getStartTime().isBefore(startOfDay)) ||
          (entity.getDate().minusDays(1).equals(date) && entity.getStartTime().isBefore(startOfDay))) {
        duration = duration.plus(getDurationBetweenStartAndEndTime(entity.getStartTime(), entity.getEndTime()));
      }
    }
    return formatDuration(duration);
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
