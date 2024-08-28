package com.example.timecraft.domain.timelog.utils;

import java.lang.reflect.InvocationTargetException;
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

  public static <B, T> T cloneTimeLogObject(B builder, Object toClone) {
    try {
      Method ticketMethod = builder.getClass().getMethod("ticket", String.class);
      ticketMethod.invoke(builder, getValue(toClone, "getTicket", String.class));

      Method descriptionMethod = builder.getClass().getMethod("description", String.class);
      descriptionMethod.invoke(builder, getValue(toClone, "getDescription", String.class));

      Method dateMethod = builder.getClass().getMethod("date", LocalDate.class);
      dateMethod.invoke(builder, getValue(toClone, "getDate", LocalDate.class));

      Method startTimeMethod = builder.getClass().getMethod("startTime", LocalTime.class);
      startTimeMethod.invoke(builder, getValue(toClone, "getStartTime", LocalTime.class));

      Method endTimeMethod = builder.getClass().getMethod("endTime", LocalTime.class);
      endTimeMethod.invoke(builder, getValue(toClone, "getEndTime", LocalTime.class));

      Method buildMethod = builder.getClass().getMethod("build");
      return (T) buildMethod.invoke(builder);

    } catch (Exception e) {
      throw new RuntimeException("Error while cloning object", e);
    }
  }

  public static <T, B> T createTimeLogObject(B builder, final LocalDate date, final LocalTime startTime, final LocalTime endTime)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    T timeLogObject = createTimeLogObject(builder, date, startTime);
    Method endTimeMethod = builder.getClass().getMethod("endTime", LocalTime.class);
    endTimeMethod.invoke(builder, endTime);
    return timeLogObject;
  }


  public static <T, B> T createTimeLogObject(B builder, final LocalDate date, final LocalTime startTime) {
    try {
      Method ticketMethod = builder.getClass().getMethod("ticket", String.class);
      ticketMethod.invoke(builder, "TMC-" + (int) (Math.random() * 1000));

      Method descriptionMethod = builder.getClass().getMethod("description", String.class);
      descriptionMethod.invoke(builder, "Description " + (int) (Math.random() * 10));

      Method dateMethod = builder.getClass().getMethod("date", LocalDate.class);
      dateMethod.invoke(builder, date);

      Method startTimeMethod = builder.getClass().getMethod("startTime", LocalTime.class);
      startTimeMethod.invoke(builder, startTime);

      Method endTimeMethod = builder.getClass().getMethod("endTime", LocalTime.class);
      endTimeMethod.invoke(builder, startTime != null ? startTime.plusHours(1) : null);

      Method buildMethod = builder.getClass().getMethod("build");
      return (T) buildMethod.invoke(builder);

    } catch (Exception e) {
      throw new RuntimeException("Error while creating object", e);
    }
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
