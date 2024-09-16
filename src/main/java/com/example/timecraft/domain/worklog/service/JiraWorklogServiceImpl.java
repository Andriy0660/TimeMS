package com.example.timecraft.domain.worklog.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.worklog.dto.WorklogJiraDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class JiraWorklogServiceImpl implements JiraWorklogService {
  public static final DateTimeFormatter JIRA_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  private final RestTemplate restTemplate;
  private final AppProperties appProperties;
  private final ObjectMapper objectMapper;
  private final SyncProgressService syncProgressService;

  @Override
  public List<WorklogJiraDto> fetchAllWorkLogDtos() {
    List<WorklogJiraDto> allWorklogJiraDtos = new ArrayList<>();
    List<String> allKeys = fetchAllIssueKeys();
    double step = 100. / allKeys.size();
    for (String key : allKeys) {
      List<WorklogJiraDto> worklogForKey = fetchWorklogDtosForIssue(key);
      allWorklogJiraDtos.addAll(worklogForKey);

      syncProgressService.setProgress(syncProgressService.getProgress() + step);
      String ticket = !worklogForKey.isEmpty() ? worklogForKey.getFirst().getTicket() : null;
      String comment = !worklogForKey.isEmpty() ? worklogForKey.getFirst().getComment() : null;
      if (ticket != null) syncProgressService.setTicketOfCurrentWorklog(ticket);
      if (comment != null) syncProgressService.setCommentOfCurrentWorklog(comment);
    }
    return allWorklogJiraDtos;
  }

  private List<String> fetchAllIssueKeys() {
    List<String> allIssueKeys = new ArrayList<>();
    int startAt = 0;
    int maxResults = 100;
    int total = 0;

    do {
      JiraResponse jiraResponse = fetchIssueKeysPage(startAt, maxResults);
      if (jiraResponse.getIssues() != null) {
        allIssueKeys.addAll(
            jiraResponse.getIssues().stream()
                .map(Issue::getKey)
                .toList()
        );
        total = jiraResponse.getTotal();
      }

      startAt += maxResults;

    } while (startAt < total);

    return allIssueKeys;
  }

  private JiraResponse fetchIssueKeysPage(int startAt, int maxResults) {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/search?&startAt=" + startAt + "&maxResults=" + maxResults;

    HttpHeaders headers = getHttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<JiraResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, JiraResponse.class);

    return response.getBody();
  }

  private HttpHeaders getHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String auth = appProperties.getJira().getEmail() + ":" + appProperties.getJira().getToken();
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    headers.set("Authorization", "Basic " + encodedAuth);
    headers.set("Content-Type", "application/json");
    return headers;
  }

  @Override
  public List<WorklogJiraDto> fetchWorklogDtosForIssue(String issueKey) {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/issue/" + issueKey + "/worklog";

    HttpHeaders headers = getHttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      String jsonResponse = response.getBody();
      return parseWorklogs(jsonResponse, issueKey);
    } else {
      throw new RuntimeException("Failed to fetch worklogs for " + issueKey + " : " + response.getStatusCode());
    }
  }

  private List<WorklogJiraDto> parseWorklogs(String jsonData, String issueKey) {
    JsonNode rootNode = null;
    try {
      rootNode = objectMapper.readTree(jsonData);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Error parsing worklog for " + issueKey);
    }
    List<WorklogJiraDto> worklogJiraDtos = new ArrayList<>();

    for (JsonNode worklogNode : rootNode.path("worklogs")) {
      WorklogJiraDto worklogJiraDto = new WorklogJiraDto();
      worklogJiraDto.setId(Long.parseLong(worklogNode.path("id").asText()));
      worklogJiraDto.setAuthor(worklogNode.path("author").path("displayName").asText());
      worklogJiraDto.setTicket(issueKey);
      worklogJiraDto.setDate(LocalDate.parse(worklogNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER));
      LocalTime startTime = LocalTime.parse(worklogNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER);
      worklogJiraDto.setStartTime(LocalTime.of(startTime.getHour(), startTime.getMinute()));
      worklogJiraDto.setComment(getTextFromAdf(worklogNode.path("comment")));
      worklogJiraDto.setTimeSpentSeconds(Integer.parseInt(worklogNode.path("timeSpentSeconds").asText()));
      worklogJiraDtos.add(worklogJiraDto);
    }

    return worklogJiraDtos;
  }

  private static String getTextFromAdf(JsonNode node) {
    StringBuilder text = new StringBuilder();

    if (node.has("content")) {
      for (JsonNode content : node.get("content")) {
        String type = content.get("type").asText();

        switch (type) {
          case "hardBreak":
            text.append("\n");
            break;
          case "paragraph":
            if (!text.isEmpty()) {
              text.append("\n");
            }
            break;
          case "listItem":
            text.append("\n- ");
            break;
          case "text":
            String textContent = content.get("text").asText();
            if (content.has("marks")) {
              for (JsonNode mark : content.get("marks")) {
                if (mark.get("type").asText().equals("code")) {
                  textContent = "`" + textContent + "`";
                  break;
                }
              }
            }
            text.append(textContent);
            break;
          default:
            break;
        }

        if (content.has("content")) {
          text.append(getTextFromAdf(content));
        }
      }
    }
    return text.toString().trim();
  }

  @Getter
  @Setter
  public static class JiraResponse {
    private List<Issue> issues;
    private Integer total;
  }

  @Getter
  @Setter
  public static class Issue {
    private String key;
  }
}
