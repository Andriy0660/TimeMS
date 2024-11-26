package com.example.timecraft.domain.jira_instance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JiraInstanceGetResponse {
  private Long id;
  private String baseUrl;
  private String email;
  private String token;
}
