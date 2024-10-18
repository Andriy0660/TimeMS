package com.example.timecraft.domain.sync;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.timecraft.config.TestPostgresContainerConfiguration;
import com.example.timecraft.domain.jira.worklog.util.JiraWorklogUtils;
import com.example.timecraft.domain.sync.jira.dto.SyncFromJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncIntoJiraRequest;
import com.example.timecraft.domain.sync.jira.dto.SyncJiraProgressResponse;
import com.example.timecraft.domain.sync.model.SyncStatus;
import com.example.timecraft.domain.sync.util.SyncApiTestUtils;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils;
import com.example.timecraft.domain.worklog.persistence.WorklogEntity;
import com.example.timecraft.domain.worklog.persistence.WorklogRepository;
import com.example.timecraft.domain.worklog.util.WorklogApiTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static com.example.timecraft.domain.sync.jira.util.SyncJiraUtils.defaultWorklogStartTime;
import static com.example.timecraft.domain.sync.util.SyncApiTestUtils.accountIdForTesting;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
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
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestPostgresContainerConfiguration.class)
@SpringBootTest
//@WireMockTest(httpPort = 9999)
public class SyncApiTest {
  @RegisterExtension
  static WireMockExtension wireMockServer = WireMockExtension.newInstance()
      .options(WireMockConfiguration.wireMockConfig()
          .port(9999)
          .notifier(new ConsoleNotifier(true)))
      .build();

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private WorklogRepository worklogRepository;

  @Autowired
  private Clock clock;

  @Autowired
  private TimeLogRepository timeLogRepository;

  @Test
  void shouldSyncFromJira() throws Exception {
    WorklogEntity worklog1 = WorklogEntity.builder()
        .id(UUID.randomUUID().getMostSignificantBits())
        .comment("comment")
        .ticket("TST-1")
        .date(LocalDate.now(clock))
        .startTime(LocalTime.of(9, 0))
        .timeSpentSeconds(3600)
        .build();

    WorklogEntity worklog2 = WorklogEntity.builder()
        .id(UUID.randomUUID().getMostSignificantBits())
        .comment("comment")
        .ticket("TST-1")
        .date(LocalDate.now(clock))
        .startTime(LocalTime.of(9, 0))
        .timeSpentSeconds(7200)
        .build();

    WorklogEntity worklog3 = WorklogEntity.builder()
        .id(UUID.randomUUID().getMostSignificantBits())
        .comment("comment")
        .ticket("TST-1")
        .date(LocalDate.now(clock))
        .startTime(LocalTime.of(9, 0))
        .timeSpentSeconds(1800)
        .build();
    worklogRepository.save(worklog1);
    worklogRepository.save(worklog2);
    worklogRepository.save(worklog3);
    int initialSize = TimeLogApiTestUtils.getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    mvc.perform(post("/syncJira/from")
            .content(objectMapper.writeValueAsString(new SyncFromJiraRequest("TST-1", LocalDate.now(clock), "comment")))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int newSize = TimeLogApiTestUtils.getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    assertEquals(initialSize + 3, newSize);
  }

  @Test
  void shouldSyncIntoJira() throws Exception {
    String ticket = "TST-1";
    String descr = "descr";
    List<TimeLogEntity> timeLogEntities = SyncApiTestUtils.createTimeLogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);
    List<WorklogEntity> worklogEntities = SyncApiTestUtils.createWorklogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);

    timeLogRepository.save(timeLogEntities.get(0));
    timeLogRepository.save(timeLogEntities.get(1));
    worklogRepository.save(worklogEntities.get(0));
    worklogRepository.save(worklogEntities.get(1));

    SyncIntoJiraRequest request = new SyncIntoJiraRequest(ticket, LocalDate.now(clock), "descr");
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
        worklogEntities.get(0).getId()
    );

    stubFor(WireMock.put(WireMock.urlMatching(".*/issue/" + request.getTicket() + "/worklog/" + worklogEntities.get(0).getId()))
        .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
        .willReturn(ok()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(responseBody)
        )
    );

    stubFor(WireMock.delete(urlMatching(".*/issue/" + "TST-1" + "/worklog/" + worklogEntities.get(1).getId()))
        .willReturn(noContent()));

    int initialSize = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    mvc.perform(post("/syncJira/to")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    int sizeAfterSyncingIntoJira = WorklogApiTestUtils.getSize(mvc, LocalDate.now(clock));
    assertEquals(initialSize, sizeAfterSyncingIntoJira + 1);
  }

  @Test
  void shouldThrowWhenSyncMismatchOnSyncIntoJira() throws Exception {
    String ticket = "TST-1";
    String descr = "descr";
    List<TimeLogEntity> timeLogEntities = SyncApiTestUtils.createTimeLogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);
    List<WorklogEntity> worklogEntities = SyncApiTestUtils.createWorklogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);

    timeLogRepository.save(timeLogEntities.get(0));
    timeLogRepository.save(timeLogEntities.get(1));
    worklogRepository.save(worklogEntities.get(0));
    worklogRepository.save(worklogEntities.get(1));

    SyncIntoJiraRequest request = new SyncIntoJiraRequest(ticket, LocalDate.now(clock), descr);
    wireMockServer.stubFor(WireMock.delete(urlMatching(".*/issue/" + ticket + "/worklog/.*"))
        .willReturn(aResponse()
            .withStatus(404)
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody("""
                {
                    "errorMessages": ["Cannot find worklog"]
                }
                """)));

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
    List<WorklogEntity> worklogEntitiesFromApp = SyncApiTestUtils.createWorklogsWithSameInfo(count, LocalDate.now(clock), ticket, appDescr);
    List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(count, LocalDate.now(clock), ticket, jiraDescr);
    for (int i = 0; i < worklogEntitiesFromApp.size() - 1; i++) {
      worklogEntitiesFromJira.get(i).setId(worklogEntitiesFromApp.get(i).getId());
    }
    worklogEntitiesFromApp.get(0).setUpdated(null);
    worklogEntitiesFromApp.get(1).setUpdated(worklogEntitiesFromJira.get(1).getUpdated().minusMinutes(30));
    worklogEntitiesFromApp.get(2).setUpdated(worklogEntitiesFromJira.get(2).getUpdated().plusMinutes(30));
    worklogRepository.saveAll(worklogEntitiesFromApp);

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
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromApp.get(0).getId() + "')].comment").value(jiraDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromApp.get(1).getId() + "')].comment").value(jiraDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromApp.get(2).getId() + "')].comment").value(appDescr))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromApp.get(3).getId() + "')]").doesNotExist());

  }

  @Test
  void shouldSyncWorklogsForTicket() throws Exception {
    String ticket = "TST-1";
    String descr = "descr";
    List<WorklogEntity> worklogEntitiesFromApp = SyncApiTestUtils.createWorklogsWithSameInfo(1, LocalDate.now(clock), ticket, descr);
    List<WorklogEntity> worklogEntitiesFromJira = SyncApiTestUtils.createWorklogsWithSameInfo(2, LocalDate.now(clock), ticket, descr);

    worklogEntitiesFromJira.get(0).setId(worklogEntitiesFromApp.get(0).getId());
    worklogEntitiesFromJira.get(0).setComment("newDescr");
    worklogEntitiesFromApp.get(0).setUpdated(worklogEntitiesFromJira.get(0).getUpdated().minusMinutes(30));

    worklogRepository.saveAll(worklogEntitiesFromApp);

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
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromApp.get(0).getId() + "')].comment").value("newDescr"))
        .andExpect(jsonPath("$.items[?(@.id == '" + worklogEntitiesFromJira.get(1).getId() + "')]").exists());

  }

  @Test
  void shouldGetNotSyncedStatus() throws Exception {
    WorklogEntity worklog1 = WorklogApiTestUtils.createWorklogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog1 = TimeLogApiTestUtils.createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);
    worklog1 = worklogRepository.save(worklog1);
    timeLog1 = timeLogRepository.save(timeLog1);

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
    String descr = "descr";
    WorklogEntity worklog1 = WorklogApiTestUtils.createWorklogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog1 = TimeLogApiTestUtils.createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    worklog1.setComment(descr);
    worklog1.setTicket(ticket);
    timeLog1.setDescription(descr);
    timeLog1.setTicket(ticket);

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);
    worklog1 = worklogRepository.save(worklog1);
    timeLog1 = timeLogRepository.save(timeLog1);

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
    String descr = "descr";
    WorklogEntity worklog1 = WorklogApiTestUtils.createWorklogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog1 = TimeLogApiTestUtils.createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog2 = TimeLogApiTestUtils.createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    worklog1.setComment(descr);
    worklog1.setTicket(ticket);
    timeLog1.setDescription(descr);
    timeLog1.setTicket(ticket);
    timeLog2.setDescription(descr);
    timeLog2.setTicket(ticket);

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);
    worklog1 = worklogRepository.save(worklog1);
    timeLog1 = timeLogRepository.save(timeLog1);
    timeLog2 = timeLogRepository.save(timeLog2);

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
