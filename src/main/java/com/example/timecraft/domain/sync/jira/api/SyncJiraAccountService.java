package com.example.timecraft.domain.sync.jira.api;

public interface SyncJiraAccountService {
  boolean checkJiraAccountExists(final String baseUrl, final String email, final String token);
}
