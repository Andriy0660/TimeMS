package com.example.timecraft.domain.sync.external_service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

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
import com.example.timecraft.domain.external_service.service.TestExternalTimeLogClient;
import com.example.timecraft.domain.external_service.util.ExternalTimeLogsApiTestUtils;
import com.example.timecraft.domain.external_timelog.dto.ExternalTimeLogCreateFromTimeLogRequest;
import com.example.timecraft.domain.external_timelog.persistence.ExternalTimeLogEntity;
import com.example.timecraft.domain.sync.external_service.dto.SyncIntoExternalServiceRequest;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TestTimeLogClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static com.example.timecraft.domain.external_service.util.ExternalTimeLogsApiTestUtils.getCreateRequest;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createTimeLogCreateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ApiTest
public class SyncExternalServiceApiTestExcludingDescription {
  private static String accessToken;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Clock clock;
  @Autowired
  private TestTimeLogClient timeLogClient;
  @Autowired
  private TestExternalTimeLogClient externalTimeLogClient;

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

    final ConfigUpdateExternalServiceRequest configRequest = new ConfigUpdateExternalServiceRequest(true, 2., false);
    mvc.perform(patch("/config/externalService")
            .content(objectMapper.writeValueAsString(configRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

  }

  @Test
  void shouldSyncIntoExternalService() throws Exception {
    TimeLogCreateRequest timeLogCreateRequest1 = createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock));
    TimeLogCreateRequest timeLogCreateRequest2 = createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock));
    timeLogClient.saveTimeLog(timeLogCreateRequest1, accessToken);
    timeLogClient.saveTimeLog(timeLogCreateRequest2, accessToken);

    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest1 = ExternalTimeLogsApiTestUtils.getCreateRequest(LocalDate.now(clock));
    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest2 = ExternalTimeLogsApiTestUtils.getCreateRequest(LocalDate.now(clock));
    externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest1, accessToken);
    externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest2, accessToken);

    SyncIntoExternalServiceRequest request = new SyncIntoExternalServiceRequest(LocalDate.now(clock), null);

    mvc.perform(post("/syncExternalService/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());
    int sizeAfterSyncing = ExternalTimeLogsApiTestUtils.getSize(mvc, LocalDate.now(clock), accessToken);
    assertThat(sizeAfterSyncing).isEqualTo(1);
  }

  @Test
  void shouldGetPartiallySyncedStatus() throws Exception {
    final TimeLogEntity timeLog = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock)), accessToken);
    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest = getCreateRequest(LocalDate.now(clock));
    final ExternalTimeLogEntity externalTimeLog = externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest, accessToken);

    final LocalDate startDate = LocalDate.now(clock);
    final LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/external-time-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + externalTimeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()));
  }

  @Test
  void shouldGetSyncedStatus() throws Exception {
    final TimeLogEntity timeLog = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock)), accessToken);
    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest = getCreateRequest(LocalDate.now(clock));
    externalTimeLogCreateRequest.setStartTime(timeLog.getStartTime());
    externalTimeLogCreateRequest.setEndTime(timeLog.getEndTime());
    final ExternalTimeLogEntity externalTimeLog = externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest, accessToken);

    final LocalDate startDate = LocalDate.now(clock);
    final LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/external-time-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + externalTimeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.SYNCED.toString()));
  }
}
