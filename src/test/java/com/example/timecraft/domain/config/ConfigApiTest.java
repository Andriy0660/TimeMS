package com.example.timecraft.domain.config;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.config.ApiTest;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateExternalServiceRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateJiraRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;
import com.example.timecraft.domain.jira_instance.dto.JiraInstanceSaveRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.JsonPath;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
public class ConfigApiTest {
  private static String accessToken;
  @Autowired
  private WireMockServer wm;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    final String email = Instancio.of(String.class).create();
    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signup")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    final AuthLogInRequest logInRequest = new AuthLogInRequest(signUpRequest.getEmail(), signUpRequest.getPassword());
    final MvcResult result = mvc.perform(post("/auth/login")
            .content(objectMapper.writeValueAsString(logInRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
  }

  @Test
  void shouldGetConfig() throws Exception {
    mvc.perform(get("/config", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isJiraEnabled").value(false))
        .andExpect(jsonPath("$.externalServiceTimeCf").value(1))
        .andExpect(jsonPath("$.dayOffsetHour").value(0));
  }

  @Test
  void shouldUpdateTimeConfig() throws Exception {
    final ConfigUpdateTimeRequest request = new ConfigUpdateTimeRequest(3, 7, 17);

    mvc.perform(patch("/config/time")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/config")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.dayOffsetHour").value(3))
        .andExpect(jsonPath("$.workingDayStartHour").value(7))
        .andExpect(jsonPath("$.workingDayEndHour").value(17));
  }

  @Test
  void shouldThrowWhenUpdatingTimeConfigWithInvalidData() throws Exception {
    final ConfigUpdateTimeRequest request = new ConfigUpdateTimeRequest(-2, 21, 0);

    mvc.perform(patch("/config/time")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetEmptyJiraInstance() throws Exception {
    mvc.perform(get("/config/jira/instance")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").isEmpty())
        .andExpect(jsonPath("$.token").isEmpty())
        .andExpect(jsonPath("$.baseUrl").isEmpty());
  }

  @Test
  void shouldSaveAndGetJiraInstance() throws Exception {
    final JiraInstanceSaveRequest jiraInstanceSaveRequest = new JiraInstanceSaveRequest(null, "http://localhost:" + wm.port(), "email@gmail.com", "token");

    wm.stubFor(WireMock.get(WireMock.urlMatching(".*/myself"))
        .willReturn(ok())
    );

    mvc.perform(post("/config/jira/instance")
            .content(objectMapper.writeValueAsString(jiraInstanceSaveRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/config/jira/instance")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(jiraInstanceSaveRequest.getEmail()))
        .andExpect(jsonPath("$.token").value(jiraInstanceSaveRequest.getToken()))
        .andExpect(jsonPath("$.baseUrl").value(jiraInstanceSaveRequest.getBaseUrl()));
  }

  @Test
  void shouldDeleteJiraInstance() throws Exception {
    final JiraInstanceSaveRequest jiraInstanceSaveRequest = new JiraInstanceSaveRequest(null, "http://localhost:" + wm.port(), "email@gmail.com", "token");

    wm.stubFor(WireMock.get(WireMock.urlMatching(".*/myself"))
        .willReturn(ok())
    );

    mvc.perform(post("/config/jira/instance")
            .content(objectMapper.writeValueAsString(jiraInstanceSaveRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    final MvcResult result = mvc.perform(get("/config/jira/instance")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(jiraInstanceSaveRequest.getEmail()))
        .andExpect(jsonPath("$.token").value(jiraInstanceSaveRequest.getToken()))
        .andExpect(jsonPath("$.baseUrl").value(jiraInstanceSaveRequest.getBaseUrl()))
        .andReturn();

    final int id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

    mvc.perform(delete("/config/jira/instance/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/config/jira/instance")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").isEmpty())
        .andExpect(jsonPath("$.token").isEmpty())
        .andExpect(jsonPath("$.baseUrl").isEmpty());
  }

  @Test
  void shouldThrowWhenUpdatingJiraConfigWithoutAnyJiraInstances() throws Exception {
    final ConfigUpdateJiraRequest request = new ConfigUpdateJiraRequest(true);

    mvc.perform(patch("/config/jira")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldUpdateJiraConfig() throws Exception {
    final JiraInstanceSaveRequest jiraInstanceSaveRequest = new JiraInstanceSaveRequest(null, "http://localhost:" + wm.port(), "email@gmail.com", "token");

    wm.stubFor(WireMock.get(WireMock.urlMatching(".*/myself"))
        .willReturn(ok())
    );

    mvc.perform(post("/config/jira/instance")
            .content(objectMapper.writeValueAsString(jiraInstanceSaveRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    final ConfigUpdateJiraRequest request = new ConfigUpdateJiraRequest(true);

    mvc.perform(patch("/config/jira")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/config")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isJiraEnabled").value(true));

  }

  @Test
  void shouldUpdateExternalServiceConfig() throws Exception {
    final ConfigUpdateExternalServiceRequest request = new ConfigUpdateExternalServiceRequest(true, 2., true);

    mvc.perform(patch("/config/external-service")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/config")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isExternalServiceEnabled").value(true))
        .andExpect(jsonPath("$.externalServiceTimeCf").value(2))
        .andExpect(jsonPath("$.isExternalServiceIncludeDescription").value(true));
  }

  @Test
  void shouldThrowWhenUpdatingExternalServiceConfigWithInvalidData() throws Exception {
    final ConfigUpdateExternalServiceRequest request = new ConfigUpdateExternalServiceRequest(true, -2., true);

    mvc.perform(patch("/config/external-service")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest());
  }
}
