package com.example.timecraft.domain.worklog.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matcher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorklogApiTestUtils {

  public static WorklogEntity createWorklogEntity(final LocalDate date, final LocalTime startTime) {
    return WorklogEntity.builder()
        .id(UUID.randomUUID().getMostSignificantBits())
        .ticket("TST-" + (int) (Math.random() * 1000))
        .comment("Description " + (int) (Math.random() * 10))
        .date(date)
        .startTime(startTime)
        .timeSpentSeconds(3600)
        .build();
  }

  public static Matcher<?> matchWorklog(final WorklogEntity entity) {
    List<Matcher<? super Map<String, String>>> matchers = new ArrayList<>();
    if (entity.getTicket() != null) matchers.add(hasEntry("ticket", entity.getTicket()));
    if (entity.getDate() != null) matchers.add(hasEntry("date", entity.getDate().toString()));
    if (entity.getStartTime() != null) matchers.add(hasEntry("startTime", entity.getStartTime().format(DateTimeFormatter.ISO_TIME)));
    if (entity.getTimeSpentSeconds() != null) matchers.add(hasEntry("timeSpentSeconds", entity.getTimeSpentSeconds()));
    if (entity.getComment() != null) matchers.add(hasEntry("comment", entity.getComment()));

    return hasItem(allOf(matchers));
  }

  public static int getSize(final MockMvc mvc, final LocalDate date) throws Exception {
    MvcResult resultBefore = mvc.perform(get("/work-logs")
            .param("date", date.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();
    return JsonPath.read(resultBefore.getResponse().getContentAsString(), "$.items.length()");
  }
}
