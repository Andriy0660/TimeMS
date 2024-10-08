package com.example.timecraft.domain.jira.worklog.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JiraWorklogMapper {
  private final ObjectMapper objectMapper;
  public static final DateTimeFormatter JIRA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");


  public List<JiraWorklogDto> parseWorklogs(final String jsonData, final String issueKey, final String accountId) {
    try {
      final JsonNode rootNode = objectMapper.readTree(jsonData);
      final List<JiraWorklogDto> worklogDtos = new ArrayList<>();

      for (JsonNode worklogNode : rootNode.path("worklogs")) {
        if (worklogNode.path("author").path("accountId").asText().equals(accountId)) {
          final JiraWorklogDto worklogDto = parseWorklog(worklogNode);
          worklogDto.setIssueKey(issueKey);
          worklogDtos.add(worklogDto);
        }
      }

      return worklogDtos;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error parsing worklogs for " + issueKey, e);
    }
  }

  public JiraWorklogDto parseWorklogResponse(final String worklogJson, final String issueKey) {
    try {
      final JsonNode worklogNode = objectMapper.readTree(worklogJson);
      final JiraWorklogDto worklogDto = parseWorklog(worklogNode);
      worklogDto.setIssueKey(issueKey);
      return worklogDto;
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error parsing worklog response for " + issueKey, e);
    }
  }

  private JiraWorklogDto parseWorklog(final JsonNode worklogNode) {
    final JiraWorklogDto worklogDto = new JiraWorklogDto();

    final LocalDateTime started = LocalDateTime.parse(worklogNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER);
    final LocalDateTime updated = LocalDateTime.parse(worklogNode.path("updated").asText(), JIRA_DATE_TIME_FORMATTER);

    worklogDto.setId(Long.parseLong(worklogNode.path("id").asText()));
    worklogDto.setAuthor(worklogNode.path("author").path("displayName").asText());
    worklogDto.setDate(started.toLocalDate());
    worklogDto.setStartTime(started.toLocalTime());
    worklogDto.setComment(JiraWorklogUtils.getTextFromAdf(worklogNode.path("comment")));
    worklogDto.setTimeSpentSeconds(worklogNode.path("timeSpentSeconds").asInt());
    worklogDto.setUpdated(updated);

    return worklogDto;
  }

  public String parseJiraAccountId(final String jsonData) {
    try {
      final JsonNode jsonNode = new ObjectMapper().readTree(jsonData);
      return jsonNode.path("accountId").asText();
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to parse Jira account id", e);
    }
  }
}