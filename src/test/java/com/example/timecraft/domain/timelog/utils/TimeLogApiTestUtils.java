package com.example.timecraft.domain.timelog.utils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TimeLogApiTestUtils {
  public static Matcher<?> matchTimeLog(final Object toCheck) {
    try {
      List<Matcher<? super Map<String, String>>> matchers = new ArrayList<>();

      String ticket = getValue(toCheck, "getTicket", String.class);
      if (ticket != null) matchers.add(hasEntry("ticket", ticket));

      LocalDate date = getValue(toCheck, "getDate", LocalDate.class);
      if (date != null) matchers.add(hasEntry("date", date.toString()));

      LocalTime startTime = getValue(toCheck, "getStartTime", LocalTime.class);
      if (startTime != null) matchers.add(hasEntry("startTime", startTime.format(DateTimeFormatter.ISO_TIME)));

      LocalTime endTime = getValue(toCheck, "getEndTime", LocalTime.class);
      if (endTime != null) matchers.add(hasEntry("endTime", endTime.format(DateTimeFormatter.ISO_TIME)));

      String description = getValue(toCheck, "getDescription", String.class);
      if (description != null) matchers.add(hasEntry("description", description));

      return hasItem(allOf(matchers));
    } catch (Exception e) {
      throw new RuntimeException("Error matching entity", e);
    }
  }

  private static <T> T getValue(final Object toCheck, final String methodName, final Class<T> returnType) {
    try {
      Method method = toCheck.getClass().getMethod(methodName);
      Object value = method.invoke(toCheck);
      return returnType.cast(value);
    } catch (Exception e) {
      return null;
    }
  }

  public static TimeLogEntity createTimeLogEntity(final LocalDate date, final LocalTime startTime) {
    return TimeLogEntity.builder()
        .ticket("TMC-" + (int) (Math.random() * 1000))
        .description("Description " + (int) (Math.random() * 10))
        .date(date)
        .startTime(startTime)
        .endTime(startTime != null ? startTime.plusHours(1) : null)
        .build();
  }

  public static TimeLogEntity createTimeLogEntity(final LocalDate date, final LocalTime startTime, final LocalTime endTime) {
    return TimeLogEntity.builder()
        .ticket("TMC-" + (int) (Math.random() * 1000))
        .description("Description " + (int) (Math.random() * 10))
        .date(date)
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }

  public static int getSize(final MockMvc mvc, final String mode, final LocalDate date, final int offset) throws Exception {
    MvcResult resultBefore = mvc.perform(get("/time-logs")
            .param("mode", mode)
            .param("date", date.toString())
            .param("offset", String.valueOf(offset))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();
    return JsonPath.read(resultBefore.getResponse().getContentAsString(), "$.items.length()");
  }

}
