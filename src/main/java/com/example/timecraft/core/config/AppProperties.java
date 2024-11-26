package com.example.timecraft.core.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private Config config;
  private Cors cors;

  @Data
  public static class Config {
    private String googleClientId;
  }

  @Data
  public static class Jira {
    private String url;
    private String email;
    private String token;
  }

  @Data
  public static class Cors {
    private List<String> allowedOrigins = new ArrayList<>();
  }
}
