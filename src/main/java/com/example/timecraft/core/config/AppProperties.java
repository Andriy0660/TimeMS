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
  private Cors cors = new Cors();

  @Data
  public static class Cors {
    private List<String> allowedOrigins = new ArrayList<>();
  }
}
