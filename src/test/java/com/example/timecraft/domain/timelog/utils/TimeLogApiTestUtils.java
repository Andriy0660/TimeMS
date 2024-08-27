package com.example.timecraft.domain.timelog.utils;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
      String ticket = getValue(toCheck, "getTicket", String.class);
      LocalDate date = getValue(toCheck, "getDate", LocalDate.class);
      LocalTime startTime = getValue(toCheck, "getStartTime", LocalTime.class);
      LocalTime endTime = getValue(toCheck, "getEndTime", LocalTime.class);
      String description = getValue(toCheck, "getDescription", String.class);

      return hasItem(allOf(
          hasEntry("ticket", ticket),
          hasEntry("date", date != null ? date.toString() : null),
          hasEntry("startTime", startTime != null ? startTime.format(DateTimeFormatter.ISO_TIME) : null),
          hasEntry("endTime", endTime != null ? endTime.format(DateTimeFormatter.ISO_TIME) : null),
          hasEntry("description", description)
      ));
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
        .endTime(startTime.plusHours(1))
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
