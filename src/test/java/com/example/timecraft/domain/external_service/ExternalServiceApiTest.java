package com.example.timecraft.domain.external_service;

import java.time.Clock;
import java.time.LocalDate;

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
import com.example.timecraft.domain.external_service.mapper.TestExternalTimeLogMapper;
import com.example.timecraft.domain.external_service.service.TestExternalTimeLogClient;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static com.example.timecraft.domain.external_service.util.ExternalTimeLogsApiTestUtils.getCreateRequest;
import static com.example.timecraft.domain.external_service.util.ExternalTimeLogsApiTestUtils.getSize;
import static com.example.timecraft.domain.external_service.util.ExternalTimeLogsApiTestUtils.matchExternalTimeLog;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
public class ExternalServiceApiTest {
  private static String accessToken;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private TestExternalTimeLogClient testExternalTimeLogClient;
  @Autowired
  private TestExternalTimeLogMapper testExternalTimeLogMapper;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Clock clock;

  @BeforeEach
  void setUp() throws Exception {
    final String email = Instancio.of(String.class).create();
    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signUp")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    final AuthLogInRequest logInRequest = new AuthLogInRequest(signUpRequest.getEmail(), signUpRequest.getPassword());
    final MvcResult result = mvc.perform(post("/auth/logIn")
            .content(objectMapper.writeValueAsString(logInRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
  }

  @Test
  void shouldListExternalTimeLogsForDay() throws Exception {
    final ExternalTimeLogCreateFromTimeLogRequest request1 = getCreateRequest(LocalDate.now(clock));
    final ExternalTimeLogCreateFromTimeLogRequest request2 = getCreateRequest(LocalDate.now(clock));

    int initialSize = getSize(mvc, LocalDate.now(clock), accessToken);

    final ExternalTimeLogEntity timeLog1 = testExternalTimeLogClient.saveExternalTimeLog(request1, accessToken);
    final ExternalTimeLogEntity timeLog2 = testExternalTimeLogMapper.fromCreateRequest(request2);

    mvc.perform(get("/external-time-logs")
            .param("date", request1.getDate().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchExternalTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", not(matchExternalTimeLog(timeLog2))));

    int sizeAfterAdding = getSize(mvc, LocalDate.now(clock), accessToken);
    assertThat(initialSize + 1).isEqualTo(sizeAfterAdding);
  }

  @Test
  void shouldCreateTimeLog() throws Exception {
    final ExternalTimeLogCreateFromTimeLogRequest request = getCreateRequest(LocalDate.now(clock));
    int initialSize = getSize(mvc, LocalDate.now(clock), accessToken);
    mvc.perform(post("/external-time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));

    int sizeAfterAdding = getSize(mvc, LocalDate.now(clock), accessToken);
    assertThat(initialSize + 1).isEqualTo(sizeAfterAdding);
  }

  @Test
  void shouldDeleteExternalTimeLog() throws Exception {
    final ExternalTimeLogCreateFromTimeLogRequest request1 = getCreateRequest(LocalDate.now(clock));

    final ExternalTimeLogEntity timeLog1 = testExternalTimeLogClient.saveExternalTimeLog(request1, accessToken);
    int initialSize = getSize(mvc, LocalDate.now(clock), accessToken);

    mvc.perform(delete("/external-time-logs/{externalTimeLogId}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    int sizeAfterDeleting = getSize(mvc, LocalDate.now(clock), accessToken);
    assertThat(initialSize).isEqualTo(sizeAfterDeleting + 1);
  }
}
