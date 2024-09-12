package com.example.timecraft.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "jira")
public class JiraProperties {
  private Jira jira;

  @Data
  public static class Jira {
    private String url;
    private String email;
    private String token;
  }
}