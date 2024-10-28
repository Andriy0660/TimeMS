package com.example.timecraft.domain.timelog.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.instancio.Instancio;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TimeLogApiTestUtils {

  public static Matcher<?> matchTimeLog(final TimeLogEntity entity) {
    List<Matcher<? super Map<String, String>>> matchers = new ArrayList<>();
    if (entity.getTicket() != null) matchers.add(hasEntry("ticket", entity.getTicket()));
    if (entity.getDate() != null) matchers.add(hasEntry("date", entity.getDate().toString()));
    if (entity.getStartTime() != null) matchers.add(hasEntry("startTime", entity.getStartTime().format(DateTimeFormatter.ISO_TIME)));
    if (entity.getEndTime() != null) matchers.add(hasEntry("endTime", entity.getEndTime().format(DateTimeFormatter.ISO_TIME)));
    if (entity.getDescription() != null) matchers.add(hasEntry("description", entity.getDescription()));

    return hasItem(allOf(matchers));
  }

  public static Matcher<?> matchTimeLogUpdateDto(final TimeLogUpdateRequest dto) {
    List<Matcher<? super Map<String, String>>> matchers = new ArrayList<>();
    if (dto.getTicket() != null) matchers.add(hasEntry("ticket", dto.getTicket()));
    if (dto.getDate() != null) matchers.add(hasEntry("date", dto.getDate().toString()));
    if (dto.getStartTime() != null) matchers.add(hasEntry("startTime", dto.getStartTime().format(DateTimeFormatter.ISO_TIME)));
    if (dto.getEndTime() != null) matchers.add(hasEntry("endTime", dto.getEndTime().format(DateTimeFormatter.ISO_TIME)));

    return hasItem(allOf(matchers));
  }

  public static Matcher<?> matchTimeLogMergeDto(final TimeLogImportRequest.TimeLogDto dto) {
    List<Matcher<? super Map<String, String>>> matchers = new ArrayList<>();
    if (dto.getTicket() != null) matchers.add(hasEntry("ticket", dto.getTicket()));
    if (dto.getDate() != null) matchers.add(hasEntry("date", dto.getDate().toString()));
    if (dto.getStartTime() != null) matchers.add(hasEntry("startTime", dto.getStartTime().format(DateTimeFormatter.ISO_TIME)));
    if (dto.getEndTime() != null) matchers.add(hasEntry("endTime", dto.getEndTime().format(DateTimeFormatter.ISO_TIME)));
    if (dto.getDescription() != null) matchers.add(hasEntry("description", dto.getDescription()));

    return hasItem(allOf(matchers));
  }

  public static TimeLogCreateRequest createTimeLogCreateRequest(final LocalDate date, final LocalTime startTime) {
    return Instancio.of(TimeLogCreateRequest.class)
        .set(field(TimeLogCreateRequest::getDate), date)
        .set(field(TimeLogCreateRequest::getStartTime), startTime)
        .create();
  }

  public static TimeLogCreateRequest createTimeLogCreateRequest(final LocalDate date, final LocalTime startTime, final String ticket, final String descr) {
    return Instancio.of(TimeLogCreateRequest.class)
        .set(field(TimeLogCreateRequest::getDate), date)
        .set(field(TimeLogCreateRequest::getStartTime), startTime)
        .set(field(TimeLogCreateRequest::getTicket), ticket)
        .set(field(TimeLogCreateRequest::getDescription), descr)
        .create();
  }

  public static TimeLogImportRequest.TimeLogDto createImportTimeLogDto(final LocalDate date, final LocalTime startTime, final LocalTime endTime) {
    return TimeLogImportRequest.TimeLogDto.builder()
        .ticket("TMC-" + (int) (Math.random() * 1000))
        .description("Description " + (int) (Math.random() * 10))
        .date(date)
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }

  public static TimeLogEntity clone(final TimeLogEntity toClone) {
    return TimeLogEntity.builder()
        .startTime(toClone.getStartTime())
        .endTime(toClone.getEndTime())
        .ticket(toClone.getTicket())
        .description(toClone.getDescription())
        .date(toClone.getDate())
        .build();
  }

  public static TimeLogEntity clone(final TimeLogImportRequest.TimeLogDto toClone) {
    return TimeLogEntity.builder()
        .startTime(toClone.getStartTime())
        .endTime(toClone.getEndTime())
        .ticket(toClone.getTicket())
        .description(toClone.getDescription())
        .date(toClone.getDate())
        .build();
  }

  public static int getSize(final MockMvc mvc, final LocalDate startDate, final LocalDate endDate) throws Exception {
    MvcResult resultBefore = mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();
    return JsonPath.read(resultBefore.getResponse().getContentAsString(), "$.items.length()");
  }

  public static String getDurationSum(String initial, String added) {
    int initialMinutes = parseDurationToMinutes(initial);
    int addedMinutes = parseDurationToMinutes(added);
    int totalMinutes = initialMinutes + addedMinutes;
    return String.format("%dh %dm", totalMinutes / 60, totalMinutes % 60);
  }

  private static int parseDurationToMinutes(String duration) {
    String[] parts = duration.split(" ");
    int hours = Integer.parseInt(parts[0].replace("h", ""));
    int minutes = Integer.parseInt(parts[1].replace("m", ""));
    return hours * 60 + minutes;
  }

}
