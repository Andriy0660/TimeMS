package com.example.timecraft.domain.sync;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import com.example.timecraft.config.IntegrationTest;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.sync.util.SyncApiTestUtils;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TestTimeLogClient;
import com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils;
import com.example.timecraft.domain.worklog.dto.WorklogCreateFromTimeLogRequest;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.service.TestWorklogClient;
import com.example.timecraft.domain.worklog.util.WorklogApiTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import static com.example.timecraft.domain.sync.util.SyncApiTestUtils.accountIdForTesting;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createTimeLogCreateRequest;
import static com.example.timecraft.domain.worklog.util.WorklogApiTestUtils.createWorklogCreateRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class SyncApiTest {
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

  @Autowired
  private TestTimeLogClient timeLogClient;

  @Test
  void shouldSyncFromJira() throws Exception {
    final String ticket = Instancio.of(String.class).create();
    final String descr = "syncfromjiradescr";
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    worklogClient.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    worklogClient.saveWorklog(request2);
    final WorklogCreateFromTimeLogRequest request3 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    worklogClient.saveWorklog(request3);

    int initialSize = TimeLogApiTestUtils.getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    mvc.perform(post("/syncJira/from")
            .content(objectMapper.writeValueAsString(new SyncFromJiraRequest(ticket, LocalDate.now(clock), descr)))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int newSize = TimeLogApiTestUtils.getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    assertThat(initialSize + 3).isEqualTo(newSize);
  }

  @Test
  void shouldSyncIntoJira() throws Exception {
    final String ticket = Instancio.of(String.class).create();
    final String descr = "syncintojiradescr";
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));
    timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    WorklogEntity worklogEntity1 = worklogClient.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    WorklogEntity worklogEntity2 = worklogClient.saveWorklog(request2);

    SyncIntoJiraRequest request = new SyncIntoJiraRequest(ticket, LocalDate.now(clock), descr);
    LocalDateTime startDateTime = LocalDateTime.of(request.getDate(), startTime);
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

    wm.stubFor(WireMock.put(WireMock.urlMatching(".*/issue/" + ticket + "/worklog/" + worklogEntity1.getId()))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(responseBody)
        )
    );

    wm.stubFor(WireMock.delete(urlMatching(".*/issue/" + ticket + "/worklog/" + worklogEntity2.getId()))
        .willReturn(noContent()));

    int initialSize = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    mvc.perform(post("/syncJira/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    int sizeAfterSyncingIntoJira = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertThat(initialSize).isEqualTo(sizeAfterSyncingIntoJira + 1);
  }

  @Test
  void shouldThrowWhenSyncMismatchOnSyncIntoJira() throws Exception {
    final String ticket = "TST-1";
    final String descr = "descr";
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));
    timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    worklogClient.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    worklogClient.saveWorklog(request2);
    worklogClient.saveWorklog(request2);

    SyncIntoJiraRequest request = new SyncIntoJiraRequest(ticket, LocalDate.now(clock), descr);
    wm.stubFor(WireMock.delete(urlMatching(".*/issue/" + ticket + "/worklog/.*"))
        .willReturn(WireMock.status(404)));

    mvc.perform(post("/syncJira/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldSyncAllWorklogs() throws Exception {
    final String ticket = "TST-1";
    final String appDescr = "appDescr";
    final String jiraDescr = "jiraDescr";
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    int count = 4;
    List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(count, LocalDate.now(clock), ticket, jiraDescr);

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), LocalTime.of(7, 0), LocalTime.of(10, 0), ticket, appDescr);
    WorklogEntity worklogFromApp1 = worklogClient.saveWorklog(request1);
    final WorklogCreateFromTimeLogRequest request2 = createWorklogCreateRequest(LocalDate.now(clock), LocalTime.of(7, 0), LocalTime.of(10, 0), ticket, appDescr);
    WorklogEntity worklogFromApp2 = worklogClient.saveWorklog(request2);
    final WorklogCreateFromTimeLogRequest request3 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, appDescr);
    WorklogEntity worklogFromApp3 = worklogClient.saveWorklog(request3);
    final WorklogCreateFromTimeLogRequest request4 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1));
    WorklogEntity worklogFromApp4 = worklogClient.saveWorklog(request4);

    worklogEntitiesFromJira.get(0).setId(worklogFromApp1.getId());
    worklogEntitiesFromJira.get(1).setId(worklogFromApp2.getId());
    worklogEntitiesFromJira.get(2).setId(worklogFromApp3.getId());
    wm.stubFor(WireMock.get(urlMatching(".*/myself"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "accountId": "%s"
                }
                """, accountIdForTesting))));

    wm.stubFor(WireMock.get(urlMatching(".*/search.*"))
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

    wm.stubFor(WireMock.get(WireMock.urlMatching(".*/issue/" + ticket + "/worklog"))
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
    assertThat(count).isEqualTo(sizeAfterSyncing);

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
    final String ticket = Instancio.of(String.class).create();
    final String descr = "descr";
    final String newDescr = "newDescr";
    final List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, newDescr);
    WorklogEntity worklogFromApp1 = worklogClient.saveWorklog(request1);
    worklogEntitiesFromJira.get(0).setId(worklogFromApp1.getId());

    wm.stubFor(WireMock.get(urlMatching(".*/myself"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "accountId": "%s"
                }
                """, accountIdForTesting))));

    wm.stubFor(WireMock.get(WireMock.urlMatching(".*/issue/" + ticket + "/worklog"))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(SyncApiTestUtils.convertListToJSONString(worklogEntitiesFromJira))
        )
    );
    int sizeBeforeSyncing = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));

    mvc.perform(post("/syncJira/{ticket}", ticket)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int sizeAfterSyncing = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertThat(sizeBeforeSyncing + 1).isEqualTo(sizeAfterSyncing);

    mvc.perform(get("/work-logs")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogFromApp1.getId() + "')].comment").value(newDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromJira.get(1).getId() + "')]").exists());

  }

  @Test
  void shouldGetNotSyncedStatus() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;
    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), "TST-1", "comment");
    WorklogEntity worklog1 = worklogClient.saveWorklog(request1);
    TimeLogEntity timeLog1 = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, "TST-2", "comment2"));


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
    final String ticket = "TST-" + (int) (Math.random() * 1000);
    final String descr = "syncdescr";
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    WorklogEntity worklog1 = worklogClient.saveWorklog(request1);
    TimeLogEntity timeLog1 = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));

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
    final String ticket = "TST-1";
    final String descr = "partsyncdescr";
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    final WorklogCreateFromTimeLogRequest request1 = createWorklogCreateRequest(LocalDate.now(clock), startTime, startTime.plusHours(1), ticket, descr);
    WorklogEntity worklog1 = worklogClient.saveWorklog(request1);
    TimeLogEntity timeLog1 = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));
    TimeLogEntity timeLog2 = timeLogClient.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime, ticket, descr));

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

    wm.stubFor(WireMock.get(urlMatching(".*/myself"))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(String.format("""
                {
                  "accountId": "%s"
                }
                """, accountIdForTesting))));

    wm.stubFor(WireMock.get(urlMatching(".*/search.*"))
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

    wm.stubFor(WireMock.get(WireMock.urlMatching(".*/issue/" + ticket + "/worklog"))
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
