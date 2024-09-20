package com.example.timecraft.domain.jira.worklog.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.core.exception.BadRequestException;
import com.example.timecraft.domain.jira.worklog.dto.JiraCreateWorklogDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogDto;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
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
    String accountId = getJiraAccountId();
    String url = appProperties.getJira().getUrl() + "/rest/api/3/issue/" + issueKey + "/worklog";

    HttpHeaders headers = getHttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);

    if (response.getStatusCode().is2xxSuccessful()) {
      String jsonResponse = response.getBody();
      return parseWorklogs(jsonResponse, issueKey, accountId);
    } else {
      throw new RuntimeException("Failed to fetch worklogs for " + issueKey + " : " + response.getStatusCode());
    }
  }

  private List<JiraWorklogDto> parseWorklogs(String jsonData, String issueKey, String accountId) {
    JsonNode rootNode = null;
    try {
      rootNode = objectMapper.readTree(jsonData);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Error parsing worklog for " + issueKey);
    }
    List<JiraWorklogDto> jiraWorklogDtos = new ArrayList<>();

    for (JsonNode worklogNode : rootNode.path("worklogs")) {
      if (!worklogNode.path("author").path("accountId").asText().equals(accountId)) {
        continue;
      }
      JiraWorklogDto jiraWorklogDto = parseJiraWorklog(worklogNode);
      jiraWorklogDto.setIssueKey(issueKey);
      jiraWorklogDtos.add(jiraWorklogDto);
    }

    return jiraWorklogDtos;
  }

  private JiraWorklogDto parseJiraWorklog(final JsonNode rootNode) {
    JiraWorklogDto jiraWorklogDto = new JiraWorklogDto();
    LocalTime startTime = LocalTime.parse(rootNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER);
    LocalDateTime updated = LocalDateTime.parse(rootNode.path("updated").asText(), JIRA_DATE_TIME_FORMATTER);

    jiraWorklogDto.setId(Long.parseLong(rootNode.path("id").asText()));
    jiraWorklogDto.setAuthor(rootNode.path("author").path("displayName").asText());
    jiraWorklogDto.setDate(LocalDate.parse(rootNode.path("started").asText(), JIRA_DATE_TIME_FORMATTER));
    jiraWorklogDto.setStartTime(LocalTime.of(startTime.getHour(), startTime.getMinute()));
    jiraWorklogDto.setComment(JiraWorklogUtils.getTextFromAdf(rootNode.path("comment")));
    jiraWorklogDto.setTimeSpentSeconds(Integer.parseInt(rootNode.path("timeSpentSeconds").asText()));
    jiraWorklogDto.setUpdated(updated);
    return jiraWorklogDto;
  }

  private String getJiraAccountId() {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/myself";
    HttpHeaders headers = getHttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Author> response = restTemplate.exchange(url, HttpMethod.GET, entity, Author.class);

    if (response.getStatusCode() == HttpStatus.OK) {
      Author author = response.getBody();
      return author.getAccountId();
    } else {
      throw new RuntimeException("Failed to fetch JIRA account id : " + response.getStatusCode());
    }
  }

  @Override
  public JiraWorklogDto create(final String issueKey, final JiraCreateWorklogDto createWorklogDto) {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/issue/" + issueKey + "/worklog";
    HttpHeaders headers = getHttpHeaders();
    HttpEntity<JiraCreateWorklogDto> entity = new HttpEntity<>(createWorklogDto, headers);

    ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

    if (response.getStatusCode() == HttpStatus.CREATED) {
      String worklogJson = response.getBody();
      try {
        JiraWorklogDto jiraWorklogDto = parseJiraWorklog(objectMapper.readTree(worklogJson));
        jiraWorklogDto.setIssueKey(issueKey);
        return jiraWorklogDto;
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error parsing worklog for " + issueKey);
      }
    } else {
      throw new RuntimeException("Failed to create worklog. Status code: " + response.getStatusCode());
    }
  }


  @Override
  public void delete(final String issueKey, final Long id) {
    String url = appProperties.getJira().getUrl() + "/rest/api/3/issue/" + issueKey + "/worklog/" + id;

    HttpHeaders headers = getHttpHeaders();

    HttpEntity<String> entity = new HttpEntity<>(headers);
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

  @Getter
  @Setter
  public static class Author {
    private String accountId;
  }
}
