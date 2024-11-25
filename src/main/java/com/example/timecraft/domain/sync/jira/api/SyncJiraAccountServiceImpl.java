package com.example.timecraft.domain.sync.jira.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncJiraAccountServiceImpl implements SyncJiraAccountService {
  private final RestTemplate restTemplate;

  @Override
  public boolean checkJiraAccountExists(final String baseUrl, final String email, final String token) {
    final HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Basic " + JiraWorklogUtils.getAuthorizationHeader(email, token));
    headers.setContentType(MediaType.APPLICATION_JSON);

    try {
      final String url = baseUrl + "/rest/api/3/myself";
      restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    } catch (HttpClientErrorException.Unauthorized e) {
      return false;
    }
    return true;
  }


}
