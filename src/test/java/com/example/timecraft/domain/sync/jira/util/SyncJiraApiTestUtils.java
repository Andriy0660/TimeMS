package com.example.timecraft.domain.sync.jira.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;

public class SyncJiraApiTestUtils {

  public static String accountIdForTesting = "accountIdForTesting";

  public static List<WorklogEntity> createWorklogsWithSameInfo(int count, LocalDate date, String ticket, String description) {
    List<WorklogEntity> worklogs = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      worklogs.add(WorklogEntity.builder()
          .id(UUID.randomUUID().getMostSignificantBits())
          .comment(description)
          .ticket(ticket)
          .date(date)
          .startTime(SyncJiraUtils.DEFAULT_WORKLOG_START_TIME)
          .updated(LocalDateTime.of(date, SyncJiraUtils.DEFAULT_WORKLOG_START_TIME))
          .timeSpentSeconds(3600)
          .build());
    }
    return worklogs;
  }

  public static String convertListToJSONString(List<WorklogEntity> worklogs) {
    String worklogsArray = worklogs.stream()
        .map(SyncJiraApiTestUtils::convertToJSONString)
        .collect(Collectors.joining(",\n", "[\n", "\n]"));

    return """
        {
            "worklogs": %s
        }
        """.formatted(worklogsArray);
  }


  public static String convertToJSONString(WorklogEntity worklogEntity) {
    LocalDateTime startDateTime = LocalDateTime.of(worklogEntity.getDate(), SyncJiraUtils.DEFAULT_WORKLOG_START_TIME);
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
