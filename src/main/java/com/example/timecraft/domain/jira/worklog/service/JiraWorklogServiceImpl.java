package com.example.timecraft.domain.jira.worklog.service;

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
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.timelog.utils.TimeLogUtils;
import com.example.timecraft.domain.worklog.service.SyncProgressService;
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
  public List<JiraWorklogDto> fetchAllWorkLogDtos() {
    List<JiraWorklogDto> allJiraWorklogDtos = new ArrayList<>();
    List<String> allKeys = fetchAllIssueKeys();
    double step = 100. / allKeys.size();
    for (String key : allKeys) {
      List<JiraWorklogDto> worklogForKey = fetchWorklogDtosForIssue(key);
      allJiraWorklogDtos.addAll(worklogForKey);

      syncProgressService.setProgress(syncProgressService.getProgress() + step);
      String ticket = !worklogForKey.isEmpty() ? worklogForKey.getFirst().getIssueKey() : null;
      String comment = !worklogForKey.isEmpty() ? worklogForKey.getFirst().getComment() : null;
      if (ticket != null) syncProgressService.setTicketOfCurrentWorklog(ticket);
      if (comment != null) syncProgressService.setCommentOfCurrentWorklog(comment);
    }
    return allJiraWorklogDtos;
  }

  private List<String> fetchAllIssueKeys() {
    List<String> allIssueKeys = new ArrayList<>();
    int startAt = 0;
    int maxResults = 100;
    int total = 0;

    do {
      JiraSearchResponse jiraResponse = fetchIssueKeysPage(startAt, maxResults);
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

  private JiraSearchResponse fetchIssueKeysPage(int startAt, int maxResults) {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/search?&startAt=" + startAt + "&maxResults=" + maxResults;

    HttpHeaders headers = getHttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<JiraSearchResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, JiraSearchResponse.class);

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
  public List<JiraWorklogDto> fetchWorklogDtosForIssue(String issueKey) {
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

  private List<JiraWorklogDto> parseWorklogs(String jsonData, String issueKey) {
    JsonNode rootNode = null;
    try {
      rootNode = objectMapper.readTree(jsonData);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Error parsing worklog for " + issueKey);
    }
    List<JiraWorklogDto> jiraWorklogDtos = new ArrayList<>();

    for (JsonNode worklogNode : rootNode.path("worklogs")) {
      JiraWorklogDto jiraWorklogDto = parseJiraWorklog(worklogNode);
      jiraWorklogDto.setIssueKey(issueKey);
      jiraWorklogDtos.add(jiraWorklogDto);
    }

    return jiraWorklogDtos;
  }

  private JiraWorklogDto parseJiraWorklog(final JsonNode rootNode) {
    JiraWorklogDto jiraWorklogDto = new JiraWorklogDto();

    jiraWorklogDto.setId(Long.parseLong(rootNode.path("id").asText()));
    jiraWorklogDto.setAuthor(rootNode.path("author").path("displayName").asText());
    jiraWorklogDto.setDate(LocalDate.parse(rootNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER));
    LocalTime startTime = LocalTime.parse(rootNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER);
    jiraWorklogDto.setStartTime(LocalTime.of(startTime.getHour(), startTime.getMinute()));
    jiraWorklogDto.setComment(TimeLogUtils.getTextFromAdf(rootNode.path("comment")));
    jiraWorklogDto.setTimeSpentSeconds(Integer.parseInt(rootNode.path("timeSpentSeconds").asText()));
    return jiraWorklogDto;
  }

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


  @Override
  public void delete(final String issueKey, final Long id) {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/issue/" + issueKey + "/worklog/" + id;

    HttpHeaders headers = getHttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);

    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("Failed to delete worklog with " + id + " for " + issueKey + " : " + response.getStatusCode());
    }
  }

  @Getter
  @Setter
  public static class JiraSearchResponse {
    private List<Issue> issues;
    private Integer total;
  }

  @Getter
  @Setter
  public static class Issue {
    private String key;
  }
}
