package com.example.timecraft.domain.sync;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.timecraft.config.MockMvcConfig;
import com.example.timecraft.config.TestPostgresContainerConfiguration;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.sync.util.SyncApiTestUtils;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TestTimeLogService;
import com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.TestWorklogService;
import com.example.timecraft.domain.worklog.util.WorklogApiTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static com.example.timecraft.domain.sync.jira.util.SyncJiraUtils.defaultWorklogStartTime;
import static com.example.timecraft.domain.sync.util.SyncApiTestUtils.accountIdForTesting;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createTimeLogCreateRequest;
import static com.example.timecraft.domain.worklog.util.WorklogApiTestUtils.createWorklogCreateRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("test")
@Import({TestPostgresContainerConfiguration.class, MockMvcConfig.class})
@SpringBootTest
@WireMockTest(httpPort = 9999)
public class SyncApiTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Clock clock;

  @Autowired
  private TestWorklogService worklogService;

  @Autowired
  private TestTimeLogService timeLogService;

  @Test
  void shouldSyncFromJira() throws Exception {
    String ticket = "TST-1";
    String descr = "syncfromjiradescr";
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    worklogService.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    worklogService.saveWorklog(request2);
    final WorklogCreateFromTimeLogRequest request3 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    worklogService.saveWorklog(request3);

    int initialSize = TimeLogApiTestUtils.getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    mvc.perform(post("/syncJira/from")
            .content(objectMapper.writeValueAsString(new SyncFromJiraRequest("TST-1", LocalDate.now(clock), descr)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int newSize = TimeLogApiTestUtils.getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    assertEquals(initialSize + 3, newSize);
  }

  @Test
  void shouldSyncIntoJira() throws Exception {
    String ticket = "TST-1";
    String descr = "syncintojiradescr";
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    WorklogEntity worklogEntity1 = worklogService.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    WorklogEntity worklogEntity2 = worklogService.saveWorklog(request2);

    SyncIntoJiraRequest request = new SyncIntoJiraRequest(ticket, LocalDate.now(clock), descr);
    LocalDateTime startDateTime = LocalDateTime.of(request.getDate(), defaultWorklogStartTime);
    String time = JiraWorklogUtils.getJiraStartedTime(startDateTime);

    String responseBody = String.format("""
                {
                    "author": {
                        "displayName": "Andrii Snovyda"
                    },
                    "comment": %s,
                    "updated": "%s",
                    "started": "%s",
                    "timeSpentSeconds": %s,
                    "id": %s
                }
            """,
        objectMapper.writeValueAsString(JiraWorklogUtils.getJiraComment(request.getDescription())),
        time,
        time,
        7200,
        worklogEntity1.getId()
    );

    stubFor(WireMock.put(WireMock.urlMatching(".*/issue/" + request.getTicket() + "/worklog/" + worklogEntity1.getId()))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(responseBody)
        )
    );

    stubFor(WireMock.delete(urlMatching(".*/issue/" + "TST-1" + "/worklog/" + worklogEntity2.getId()))
        .willReturn(noContent()));

    int initialSize = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    mvc.perform(post("/syncJira/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    int sizeAfterSyncingIntoJira = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertEquals(initialSize, sizeAfterSyncingIntoJira + 1);
  }

  @Disabled
  @Test
  void shouldThrowWhenSyncMismatchOnSyncIntoJira() throws Exception {
    String ticket = "TST-1";
    String descr = "descr";

    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    worklogService.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    worklogService.saveWorklog(request2);
    worklogService.saveWorklog(request2);

    SyncIntoJiraRequest request = new SyncIntoJiraRequest(ticket, LocalDate.now(clock), descr);
    stubFor(WireMock.delete(urlMatching(".*/issue/" + ticket + "/worklog/.*"))
        .willReturn(WireMock.status(404)));

    mvc.perform(post("/syncJira/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldSyncAllWorklogs() throws Exception {
    String ticket = "TST-1";
    String appDescr = "appDescr";
    String jiraDescr = "jiraDescr";

    int count = 4;
    List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(count, LocalDate.now(clock), ticket, jiraDescr);

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), LocalTime.of(7, 0), LocalTime.of(10, 0), ticket, appDescr);
    WorklogEntity worklogFromApp1 = worklogService.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), LocalTime.of(7, 0), LocalTime.of(10, 0), ticket, appDescr);
    WorklogEntity worklogFromApp2 = worklogService.saveWorklog(request2);
    final WorklogCreateFromTimeLogRequest request3 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, appDescr);
    WorklogEntity worklogFromApp3 = worklogService.saveWorklog(request3);
    final WorklogCreateFromTimeLogRequest request4 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1));
    WorklogEntity worklogFromApp4 = worklogService.saveWorklog(request4);

    worklogEntitiesFromJira.get(0).setId(worklogFromApp1.getId());
    worklogEntitiesFromJira.get(1).setId(worklogFromApp2.getId());
    worklogEntitiesFromJira.get(2).setId(worklogFromApp3.getId());
    stubFor(WireMock.get(urlMatching(".*/myself"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "accountId": "%s"
                }
                """, accountIdForTesting))));

    stubFor(WireMock.get(urlMatching(".*/search.*"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "issues": [{
                    "key": "%s",
                    "fields": {
                      "timeoriginalestimate": 0,
                      "timespent": %s
                    }
                  }],
                  "total": %s
                }
                """, ticket, worklogEntitiesFromJira.get(0).getTimeSpentSeconds() * count, 1))));

    stubFor(WireMock.get(WireMock.urlMatching(".*/issue/" + ticket + "/worklog"))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(SyncApiTestUtils.convertListToJSONString(worklogEntitiesFromJira))
        )
    );

    mvc.perform(post("/syncJira/syncAllWorklogs")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int sizeAfterSyncing = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertEquals(count, sizeAfterSyncing);

    mvc.perform(get("/work-logs")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogFromApp1.getId() + "')].comment").value(jiraDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogFromApp2.getId() + "')].comment").value(jiraDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogFromApp4.getId() + "')]").doesNotExist());
  }

  @Test
  void shouldSyncWorklogsForTicket() throws Exception {
    String ticket = "TST-1";
    String descr = "descr";
    String newDescr = "newDescr";
    List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, newDescr);
    WorklogEntity worklogFromApp1 = worklogService.saveWorklog(request1);
    worklogEntitiesFromJira.get(0).setId(worklogFromApp1.getId());

    stubFor(WireMock.get(urlMatching(".*/myself"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "accountId": "%s"
                }
                """, accountIdForTesting))));

    stubFor(WireMock.get(WireMock.urlMatching(".*/issue/" + ticket + "/worklog"))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(SyncApiTestUtils.convertListToJSONString(worklogEntitiesFromJira))
        )
    );

    mvc.perform(post("/syncJira/{ticket}", ticket)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int sizeAfterSyncing = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertEquals(2, sizeAfterSyncing);

    mvc.perform(get("/work-logs")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogFromApp1.getId() + "')].comment").value(newDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromJira.get(1).getId() + "')]").exists());

  }

  @Test
  void shouldGetNotSyncedStatus() throws Exception {
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), "TST-1", "comment");
    WorklogEntity worklog1 = worklogService.saveWorklog(request1);
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, "TST-2", "comment2"));


    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/work-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + worklog1.getId() + "')].jiraSyncInfo.status").value(SyncStatus.NOT_SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog1.getId() + "')].jiraSyncInfo.status").value(SyncStatus.NOT_SYNCED.toString()));
  }

  @Test
  void shouldGetSyncedStatus() throws Exception {
    String ticket = "TST-" + (int) (Math.random() * 1000);
    String descr = "syncdescr";
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    WorklogEntity worklog1 = worklogService.saveWorklog(request1);
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/work-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + worklog1.getId() + "')].jiraSyncInfo.status").value(SyncStatus.SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog1.getId() + "')].jiraSyncInfo.status").value(SyncStatus.SYNCED.toString()));
  }

  @Test
  void shouldGetPartiallySyncedStatus() throws Exception {
    String ticket = "TST-1";
    String descr = "partsyncdescr";
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, defaultWorklogStartTime.plusHours(1), ticket, descr);
    WorklogEntity worklog1 = worklogService.saveWorklog(request1);
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime, ticket, descr));

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);
    mvc.perform(get("/work-logs")
            .param("date", startDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + worklog1.getId() + "')].jiraSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog1.getId() + "')].jiraSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()))
        .andExpect(jsonPath("$.items[?(@.id == '" + timeLog2.getId() + "')].jiraSyncInfo.status").value(SyncStatus.PARTIAL_SYNCED.toString()));
  }

  @Test
  void shouldShowCompletedProgress() throws Exception {
    String ticket = "TST-1";
    int count = 80;
    List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(count, LocalDate.now(clock), ticket, "descr");

    stubFor(WireMock.get(urlMatching(".*/myself"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "accountId": "%s"
                }
                """, accountIdForTesting))));

    stubFor(WireMock.get(urlMatching(".*/search.*"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                    {
                      "issues": [{
                        "key": "%s",
                        "fields": {
                          "timeoriginalestimate": %s,
                          "timespent": %s
                        }
                      }],
                      "total": %s
                    }
                    """, ticket,
                worklogEntitiesFromJira.get(0).getTimeSpentSeconds() * count * 2,
                worklogEntitiesFromJira.get(0).getTimeSpentSeconds() * count,
                1))));

    stubFor(WireMock.get(WireMock.urlMatching(".*/issue/" + ticket + "/worklog"))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(SyncApiTestUtils.convertListToJSONString(worklogEntitiesFromJira))
        )
    );

    mvc.perform(post("/syncJira/syncAllWorklogs")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    await()
        .atMost(30, TimeUnit.SECONDS)
        .pollInterval(1, TimeUnit.SECONDS)
        .until(() -> {
          MockHttpServletResponse response = mvc.perform(get("/syncJira/progress")
                  .contentType(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();

          SyncJiraProgressResponse progress = objectMapper.readValue(response.getContentAsString(), SyncJiraProgressResponse.class);

          return !progress.isInProgress() && progress.getProgress() == 100.0;
        });

    MockHttpServletResponse finalResponse = mvc.perform(get("/syncJira/progress")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse();

    SyncJiraProgressResponse progressResponse = objectMapper.readValue(finalResponse.getContentAsString(), SyncJiraProgressResponse.class);

    assertThat(progressResponse.isInProgress()).isFalse();
    assertThat(progressResponse.getProgress()).isEqualTo(100.0);
    assertThat(progressResponse.getTotalIssues()).isGreaterThan(0);
    assertThat(progressResponse.getTotalTimeSpent()).isEqualTo("10d 0h");
    assertThat(progressResponse.getTotalEstimate()).isEqualTo("20d 0h");
    assertThat(progressResponse.getCurrentIssueNumber()).isEqualTo(progressResponse.getTotalIssues());
    assertThat(progressResponse.getWorklogInfos()).isNotEmpty();
  }
}
