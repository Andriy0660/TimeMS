package com.example.timecraft.domain.worklog;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.config.IntegrationTest;
import com.example.timecraft.config.WireMockConfig;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogResponse;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.TestWorklogClient;
import com.example.timecraft.domain.worklog.util.WorklogApiTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.example.timecraft.domain.worklog.util.WorklogApiTestUtils.createWorklogCreateRequest;
import static com.example.timecraft.domain.worklog.util.WorklogApiTestUtils.getSize;
import static com.example.timecraft.domain.worklog.util.WorklogApiTestUtils.matchWorklog;
import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@Import(WireMockConfig.class)
public class WorklogApiTest {

  @Autowired
  private WireMockServer wm;

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Clock clock;

  @Autowired
  private TestWorklogClient worklogClient;

  @Test
  void shouldListWorklogsForDay() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1));
    WorklogEntity worklog1 = worklogClient.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1));
    WorklogEntity worklog2 = worklogClient.saveWorklog(request2);
    final WorklogCreateFromTimeLogRequest request3 = createWorklogCreateRequest(LocalDate.now(clock).plusDays(1), startTime, startTime.plusHours(1));
    WorklogEntity worklog3 = worklogClient.saveWorklog(request3);

    LocalDate date = LocalDate.now(clock);

    mvc.perform(get("/work-logs")
            .param("date", date.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchWorklog(worklog1)))
        .andExpect(jsonPath("$.items", matchWorklog(worklog2)))
        .andExpect(jsonPath("$.items", not(matchWorklog(worklog3))));
  }

  @Test
  void shouldCreateFromTimeLog() throws Exception {
    WorklogCreateFromTimeLogRequest request = new WorklogCreateFromTimeLogRequest(
        "TST-2",
        LocalDate.now(clock),
        LocalTime.of(9, 0),
        LocalTime.of(11, 0),
        "This is a sample description\nanother line"
    );

    wm.stubFor(WireMock.post(WireMock.urlMatching(".*/issue/" + request.getTicket() + "/worklog"))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(created()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(WorklogApiTestUtils.generateWorklogResponseBody(request, objectMapper))
        )
    );

    int initialSize = getSize(mvc, LocalDate.now(clock));
    MvcResult result = mvc.perform(post("/work-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ticket").value(request.getTicket()))
        .andExpect(jsonPath("$.comment").value(request.getDescription()))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    WorklogCreateFromTimeLogResponse response = objectMapper.readValue(content, WorklogCreateFromTimeLogResponse.class);

    int sizeAfterAdding = getSize(mvc, LocalDate.now(clock));
    assertThat(initialSize + 1).isEqualTo(sizeAfterAdding);
  }

  @Test
  void shouldDeleteWorklog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;
    final WorklogCreateFromTimeLogRequest request = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1));
    WorklogEntity worklog = worklogClient.saveWorklog(request);
    wm.stubFor(WireMock.delete(urlMatching(".*/issue/" + worklog.getTicket() + "/worklog/" + worklog.getId()))
        .willReturn(noContent()));

    int initialSize = getSize(mvc, LocalDate.now(clock));

    mvc.perform(delete("/work-logs/{issueKey}/{worklogId}", worklog.getTicket(), worklog.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int sizeAfterDeleting = getSize(mvc, LocalDate.now(clock));
    assertThat(initialSize).isEqualTo(sizeAfterDeleting + 1);
  }

  @Test
  void shouldThrowWhenIsAlreadyDeleted() throws Exception {
    WorklogEntity worklog = WorklogEntity.builder().id(UUID.randomUUID().getMostSignificantBits()).ticket("TST-2").build();
    wm.stubFor(WireMock.delete(urlMatching(".*/issue/" + worklog.getTicket() + "/worklog/" + worklog.getId()))
        .willReturn(notFound()));

    mvc.perform(delete("/work-logs/{issueKey}/{worklogId}", worklog.getTicket(), worklog.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

  }

}
