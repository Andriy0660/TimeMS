package com.example.timecraft.domain.jira.worklog.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.timecraft.core.exception.NotFoundException;
import com.example.timecraft.core.exception.UnauthorizedException;
import com.example.timecraft.domain.jira.worklog.dto.JiraSearchResponse;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogCreateDto;
import com.example.timecraft.domain.jira.worklog.dto.JiraWorklogUpdateDto;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.jira_instance.api.UserJiraInstanceService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JiraWorklogHttpClient {
  private final RestTemplate restTemplate;
  private final UserJiraInstanceService userJiraInstanceService;

  public String getWorklogsForIssue(final String issueKey) {
    final String url = buildUrl("/issue/" + issueKey + "/worklog");
    try {
      final ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, getHttpEntity(), String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        return response.getBody();
      } else {
        throw new RuntimeException("Failed to fetch worklogs for " + issueKey + " : " + response.getStatusCode());
      }
    } catch (HttpClientErrorException.NotFound e) {
      throw new NotFoundException("Issue " + issueKey + " not found");
    }
  }

  public String createWorklog(final String issueKey, final JiraWorklogCreateDto createDto) {
    final String url = buildUrl("/issue/" + issueKey + "/worklog");
    try {
      final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, getHttpEntity(createDto), String.class);
      if (response.getStatusCode() == HttpStatus.CREATED) {
        return response.getBody();
      } else {
        throw new RuntimeException("Failed to delete worklog for " + issueKey + " : " + response.getStatusCode());
      }
    } catch (HttpClientErrorException.NotFound e) {
      throw new NotFoundException("Issue " + issueKey + " not found");
    }
  }

  public String updateWorklog(final String issueKey, final Long id, final JiraWorklogUpdateDto updateDto) {
    final String url = buildUrl("/issue/" + issueKey + "/worklog/" + id);
    try {
      final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, getHttpEntity(updateDto), String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        return response.getBody();
      } else {
        throw new RuntimeException("Failed to update worklog with " + id + " for " + issueKey + " : " + response.getStatusCode());
      }
    } catch (HttpClientErrorException.NotFound e) {
      throw new NotFoundException("Issue key or worklog id is not found");
    }
  }

  public void deleteWorklog(final String issueKey, final Long id) {
    final String url = buildUrl("/issue/" + issueKey + "/worklog/" + id);
    try {
      final ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, getHttpEntity(), Void.class);

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new RuntimeException("Failed to delete worklog with " + id + " for " + issueKey + " : " + response.getStatusCode());
      }
    } catch (HttpClientErrorException.NotFound e) {
      throw new NotFoundException("Issue key or worklog id is not found");
    }
  }

  public String getJiraAccountId() {
    try {
      final String url = buildUrl("/myself");
      final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), String.class);

      if (response.getStatusCode() == HttpStatus.OK) {
        return response.getBody();
      } else {
        throw new RuntimeException("Failed to fetch Jira account id : " + response.getStatusCode());
      }
    } catch (HttpClientErrorException.Unauthorized e) {
      throw new UnauthorizedException("You must be authorized into your Jira account");
    }
  }

  public JiraSearchResponse searchIssues(final int startAt, final int maxResults) {
    final String url = buildUrl("/search?startAt=" + startAt + "&maxResults=" + maxResults);
    final ResponseEntity<JiraSearchResponse> response = restTemplate.exchange(url, HttpMethod.GET, getHttpEntity(), JiraSearchResponse.class);

    return response.getBody();
  }

  private String buildUrl(final String path) {
    final String baseUrl = userJiraInstanceService.getJiraInstance().getBaseUrl();
    return baseUrl + "/rest/api/3" + path;
  }

  private HttpEntity<String> getHttpEntity() {
    final HttpHeaders headers = getHeaders();
    return new HttpEntity<>(headers);
  }

  private <T> HttpEntity<T> getHttpEntity(final T dto) {
    final HttpHeaders headers = getHeaders();
    return new HttpEntity<>(dto, headers);
  }

  public HttpHeaders getHeaders() {
    final HttpHeaders headers = new HttpHeaders();
    final String email = userJiraInstanceService.getJiraInstance().getEmail();
    final String token = userJiraInstanceService.getJiraInstance().getToken();
    headers.set("Authorization", "Basic " + JiraWorklogUtils.getAuthorizationHeader(email, token));
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}