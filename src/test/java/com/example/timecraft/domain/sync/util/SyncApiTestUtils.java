package com.example.timecraft.domain.sync.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

import static com.example.timecraft.domain.sync.jira.util.SyncJiraUtils.defaultWorklogStartTime;

public class SyncApiTestUtils {

  public static String accountIdForTesting = "accountIdForTesting";

  public static List<TimeLogEntity> createTimeLogsWithSameInfo(int count, LocalDate date, String ticket, String description) {
    List<TimeLogEntity> timeLogs = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      timeLogs.add(TimeLogEntity.builder()
          .id(UUID.randomUUID().getMostSignificantBits())
          .description(description)
          .ticket(ticket)
          .date(date)
          .startTime(LocalTime.of(9, 0))
          .endTime(LocalTime.of(10, 0))
          .build());
    }
    return timeLogs;
  }

  public static List<WorklogEntity> createWorklogsWithSameInfo(int count, LocalDate date, String ticket, String description) {
    List<WorklogEntity> timeLogs = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      timeLogs.add(WorklogEntity.builder()
          .id(UUID.randomUUID().getMostSignificantBits())
          .comment(description)
          .ticket(ticket)
          .date(date)
          .startTime(defaultWorklogStartTime)
          .updated(LocalDateTime.of(date, defaultWorklogStartTime))
          .timeSpentSeconds(3600)
          .build());
    }
    return timeLogs;
  }

  public static String convertListToJSONString(List<WorklogEntity> worklogs) {
    String worklogsArray = worklogs.stream()
        .map(SyncApiTestUtils::convertToJSONString)
        .collect(Collectors.joining(",\n", "[\n", "\n]"));

    return """
        {
            "worklogs": %s
        }
        """.formatted(worklogsArray);
  }


  public static String convertToJSONString(WorklogEntity worklogEntity) {
    LocalDateTime startDateTime = LocalDateTime.of(worklogEntity.getDate(), defaultWorklogStartTime);
    String time = JiraWorklogUtils.getJiraStartedTime(startDateTime);

    try {
      return """
          {
              "author": {
                  "displayName": "%s",
                  "accountId": "%s"
              },
              "comment": %s,
              "updated": "%s",
              "started": "%s",
              "timeSpentSeconds": %s,
              "id": %s
          }
          """.formatted(
          worklogEntity.getAuthor(), // displayName
          accountIdForTesting, //accountId
          new ObjectMapper().writeValueAsString(JiraWorklogUtils.getJiraComment(worklogEntity.getComment())), // comment
          time, // updated
          time, // started
          worklogEntity.getTimeSpentSeconds(), // timeSpentSeconds
          worklogEntity.getId() // id
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
