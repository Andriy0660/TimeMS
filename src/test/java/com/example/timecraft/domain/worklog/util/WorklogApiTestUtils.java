package com.example.timecraft.domain.worklog.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.Matcher;
import org.instancio.Instancio;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.instancio.Select.field;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WorklogApiTestUtils {

  public static WorklogCreateFromTimeLogRequest createWorklogCreateRequest(final LocalDate date, final LocalTime startTime, final LocalTime endTime) {
    return Instancio.of(WorklogCreateFromTimeLogRequest.class)
        .set(field(WorklogCreateFromTimeLogRequest::getDate), date)
        .set(field(WorklogCreateFromTimeLogRequest::getStartTime), startTime)
        .set(field(WorklogCreateFromTimeLogRequest::getEndTime), endTime)
        .create();
  }

  public static WorklogCreateFromTimeLogRequest createWorklogCreateRequest(final LocalDate date, final LocalTime startTime, final LocalTime endTime, final String ticket, final String comment) {
    return Instancio.of(WorklogCreateFromTimeLogRequest.class)
        .set(field(WorklogCreateFromTimeLogRequest::getDate), date)
        .set(field(WorklogCreateFromTimeLogRequest::getDescription), comment)
        .set(field(WorklogCreateFromTimeLogRequest::getTicket), ticket)
        .set(field(WorklogCreateFromTimeLogRequest::getStartTime), startTime)
        .set(field(WorklogCreateFromTimeLogRequest::getEndTime), endTime)
        .create();
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

  public static String generateWorklogResponseBody(final WorklogCreateFromTimeLogRequest request, final ObjectMapper objectMapper) throws Exception {
    final LocalDateTime startDateTime = LocalDateTime.of(request.getDate(), request.getStartTime());
    final String time = JiraWorklogUtils.getJiraStartedTime(startDateTime);
    final long id = UUID.randomUUID().getMostSignificantBits();
    final long timeSpentSeconds = Duration.between(request.getStartTime(), request.getEndTime()).toSeconds();
    final String comment = objectMapper.writeValueAsString(JiraWorklogUtils.getJiraComment(request.getDescription()));

    return String.format("""
                {
                    "author": {
                        "displayName": "Andrii Snovyda"
                    },
                    "comment": %s,
                    "updated": "%s",
                    "started": "%s",
                    "timeSpentSeconds": %s,
                    "id": %s
                }
            """,
        comment,
        time,
        time,
        timeSpentSeconds,
        id
    );
  }

}
