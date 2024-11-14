package com.example.timecraft.domain.sync.external_service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.example.timecraft.config.ApiTest;
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

import static com.example.timecraft.domain.external_service.util.ExternalTimeLogsApiTestUtils.getCreateRequest;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createTimeLogCreateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ApiTest
@TestPropertySource(properties = "app.config.external-service-include-description=false")
public class SyncExternalServiceApiTestExcludingDescription {
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

  @Test
  void shouldSyncIntoExternalService() throws Exception {
    TimeLogCreateRequest timeLogCreateRequest1 = createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock));
    TimeLogCreateRequest timeLogCreateRequest2 = createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock));
    timeLogClient.saveTimeLog(timeLogCreateRequest1);
    timeLogClient.saveTimeLog(timeLogCreateRequest2);

    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest1 = ExternalTimeLogsApiTestUtils.getCreateRequest(LocalDate.now(clock));
    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest2 = ExternalTimeLogsApiTestUtils.getCreateRequest(LocalDate.now(clock));
    externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest1);
    externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest2);

    SyncIntoExternalServiceRequest request = new SyncIntoExternalServiceRequest(LocalDate.now(clock), null);

    mvc.perform(post("/syncExternalService/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    int sizeAfterSyncing = ExternalTimeLogsApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertThat(sizeAfterSyncing).isEqualTo(1);
  }

  @Test
  void shouldGetPartiallySyncedStatus() throws Exception {
    final TimeLogEntity timeLog = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock)));
    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest = getCreateRequest(LocalDate.now(clock));
    final ExternalTimeLogEntity externalTimeLog = externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest);

    final LocalDate startDate = LocalDate.now(clock);
    final LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/external-time-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + externalTimeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()));
  }

  @Test
  void shouldGetSyncedStatus() throws Exception {
    final TimeLogEntity timeLog = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.now(clock)));
    final ExternalTimeLogCreateFromTimeLogRequest externalTimeLogCreateRequest = getCreateRequest(LocalDate.now(clock));
    externalTimeLogCreateRequest.setStartTime(timeLog.getStartTime());
    externalTimeLogCreateRequest.setEndTime(timeLog.getEndTime());
    final ExternalTimeLogEntity externalTimeLog = externalTimeLogClient.saveExternalTimeLog(externalTimeLogCreateRequest);

    final LocalDate startDate = LocalDate.now(clock);
    final LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/external-time-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + externalTimeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog.getId() + "')].externalServiceSyncInfo.status").value(SyncStatus.SYNCED.toString()));
  }
}
