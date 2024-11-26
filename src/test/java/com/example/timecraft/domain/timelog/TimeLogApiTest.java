package com.example.timecraft.domain.timelog;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.config.ApiTest;
import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.config.dto.ConfigUpdateTimeRequest;
import com.example.timecraft.domain.sync.jira.util.SyncJiraUtils;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TestTimeLogClient;
import com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createImportTimeLogDto;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createTimeLogCreateRequest;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.getDurationSum;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.getSize;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.matchTimeLog;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.matchTimeLogMergeDto;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.matchTimeLogUpdateDto;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.time.temporal.TemporalAdjusters.next;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
class TimeLogApiTest {

  private static String accessToken;
  @Autowired
  private MockMvc mvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private Clock clock;
  @Autowired
  private AppProperties props;
  @Autowired
  private TestTimeLogClient timeLogService;

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

    final ConfigUpdateTimeRequest configRequest = new ConfigUpdateTimeRequest(3, 9, 18);
    mvc.perform(patch("/config/time")
            .content(objectMapper.writeValueAsString(configRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());
  }

  @Test
  void shouldReturnTimeLogsForDayMode() throws Exception {
    final LocalDate startDate = LocalDate.now(clock);
    final LocalDate endDate = LocalDate.now(clock).plusDays(1);
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).plusDays(1),
        LocalTime.of(2, 0, 0)), accessToken);
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).plusDays(2),
        LocalTime.of(10, 0, 0)), accessToken);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldReturnTimeLogsForWeekMode() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).with(next(DayOfWeek.MONDAY)),
        LocalTime.of(2, 0, 0)), accessToken);
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).plusDays(8),
        LocalTime.of(10, 0, 0)), accessToken);

    LocalDate startDate = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endDate = LocalDate.now(clock).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldReturnTimeLogsForMonthMode() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).with(firstDayOfNextMonth()),
        LocalTime.of(2, 0, 0)), accessToken);
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).with(firstDayOfNextMonth()).plusDays(1),
        LocalTime.of(10, 0, 0)), accessToken);

    LocalDate startDate = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endDate = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }


  @Test
  void shouldGetBadRequestWhenInvalidType() throws Exception {
    mvc.perform(get("/time-logs")
            .param("startDate", "invalid-type")
            .param("endDate", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid argument type of parameter: startDate"));
  }

  @Test
  void shouldCreateTimeLog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessToken);

    TimeLogCreateRequest request = new TimeLogCreateRequest("TMC-1", LocalDate.now(clock), startTime, "some descr");

    mvc.perform(post("/time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    int newSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessToken);
    assertThat(initialSize + 1).isEqualTo(newSize);
  }

  @Test
  void shouldCreateTimeLogFromWorklog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessToken);

    TimeLogCreateFromWorklogRequest request = new TimeLogCreateFromWorklogRequest("TST-2", LocalDate.now(clock), startTime, "some descr", 3600);

    mvc.perform(post("/time-logs/from-worklog")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    int newSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessToken);
    assertThat(initialSize + 1).isEqualTo(newSize);
  }

  @Test
  void shouldGetBadRequestProvidingNotCreateRequest() throws Exception {
    mvc.perform(post("/time-logs")
            .content(objectMapper.writeValueAsString(null))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetTimeLog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ticket").value(timeLog1.getTicket()))
        .andExpect(jsonPath("$.description").value(timeLog1.getDescription()));
  }

  @Test
  void shouldGetNotFoundPassingNonExistingId() throws Exception {
    mvc.perform(get("/time-logs/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Time log with such id does not exist"));
  }

  @Test
  void shouldMergeTimeLogs() throws Exception {
    TimeLogImportRequest.TimeLogDto timeLogDto1 = createImportTimeLogDto(LocalDate.now(clock), LocalTime.of(9, 0, 0),
        LocalTime.of(10, 0, 0));
    TimeLogImportRequest.TimeLogDto timeLogDto2 = createImportTimeLogDto(LocalDate.now(clock), LocalTime.of(11, 0, 0),
        LocalTime.of(12, 0, 0));
    TimeLogImportRequest.TimeLogDto timeLogDto3 = createImportTimeLogDto(LocalDate.now(clock), LocalTime.of(13, 0, 0),
        LocalTime.of(14, 0, 0));
    TimeLogImportRequest.TimeLogDto timeLogDto4 = createImportTimeLogDto(LocalDate.now(clock), LocalTime.of(15, 0, 0),
        LocalTime.of(15, 0, 0));

    timeLogService.saveTimeLog(createTimeLogCreateRequest(timeLogDto1.getDate(), timeLogDto1.getStartTime(), timeLogDto1.getTicket(), timeLogDto1.getDescription()), accessToken);
    timeLogService.saveTimeLog(createTimeLogCreateRequest(timeLogDto2.getDate(), timeLogDto2.getStartTime(), timeLogDto2.getTicket(), timeLogDto2.getDescription()), accessToken);

    TimeLogImportRequest request = TimeLogImportRequest.builder()
        .dateGroups(Arrays.asList(
            TimeLogImportRequest.TimeLogDateGroup.builder()
                .key(LocalDate.now(clock))
                .items(Arrays.asList(timeLogDto1, timeLogDto2))
                .build(),
            TimeLogImportRequest.TimeLogDateGroup.builder()
                .key(LocalDate.now(clock).plusDays(1))
                .items(Arrays.asList(timeLogDto3, timeLogDto4))
                .build()
        )).build();
    LocalDate startDate = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endDate = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
    int initialSize = getSize(mvc, startDate, endDate, accessToken);

    mvc.perform(post("/time-logs/import-time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());
    int newSize = getSize(mvc, startDate, endDate, accessToken);

    assertThat(initialSize + 2).isEqualTo(newSize);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLogMergeDto(timeLogDto3)))
        .andExpect(jsonPath("$.items", matchTimeLogMergeDto(timeLogDto4)));

  }

  @Test
  void shouldDivideTimeLog() throws Exception {
    TimeLogEntity timeLog = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(11, 0, 0)), LocalTime.of(5, 0), accessToken);
    final LocalDate today = LocalDate.now(clock);
    final LocalDate tomorrow = today.plusDays(1);
    int initialSizeOfToday = getSize(mvc, today, tomorrow, accessToken);
    int initialSizeOfTomorrow = getSize(mvc, tomorrow, tomorrow, accessToken);

    mvc.perform(post("/time-logs/divide/{timeLogId}", timeLog.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());
    int newSizeOfToday = getSize(mvc, today, tomorrow, accessToken);
    int newSizeOfTomorrow = getSize(mvc, tomorrow, tomorrow, accessToken);
    assertThat(initialSizeOfToday).isEqualTo(newSizeOfToday);
    assertThat(initialSizeOfTomorrow + 1).isEqualTo(newSizeOfTomorrow);
  }

  @Test
  void shouldGetHoursForWeek() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;
    final LocalDate monday = LocalDate.now(clock).with(DayOfWeek.MONDAY);
    final LocalDate sunday = LocalDate.now().with(DayOfWeek.SUNDAY);

    MvcResult initialResult = mvc.perform(get("/time-logs/week/hours")
            .param("date", LocalDate.now(clock).toString())
            .param("includeTickets", "false")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode initialResponse = objectMapper.readTree(initialResult.getResponse().getContentAsString());

    String initialMondayDuration = "0h 0m";
    String initialSundayDuration = "0h 0m";
    boolean initialSundayConflicted = false;

    JsonNode items = initialResponse.get("items");
    for (JsonNode item : items) {
      String dayName = item.get("dayName").asText();
      if ("Monday".equals(dayName)) {
        initialMondayDuration = item.get("duration").asText();
      } else if ("Sunday".equals(dayName)) {
        initialSundayDuration = item.get("duration").asText();
        initialSundayConflicted = item.has("conflicted") && item.get("conflicted").asBoolean();
      }
    }

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(monday, startTime), accessToken);
    TimeLogEntity timeLog12 = timeLogService.saveTimeLog(createTimeLogCreateRequest(monday, startTime.plusMinutes(30)), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(sunday, startTime), accessToken);

    String expectedMondayDuration = getDurationSum(initialMondayDuration, "2h 0m");
    String expectedSundayDuration = getDurationSum(initialSundayDuration, "1h 0m");

    mvc.perform(get("/time-logs/week/hours")
            .param("date", LocalDate.now(clock).toString())
            .param("includeTickets", "false")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.dayName == 'Monday' && @.duration == '" + expectedMondayDuration + "' && @.conflicted == true)]").exists())
        .andExpect(jsonPath("$.items[?(@.dayName == 'Sunday' && @.duration == '" + expectedSundayDuration + "' && @.conflicted == " +
            (initialSundayConflicted) + ")]").exists());
  }

  @Test
  void shouldGetHoursForWeekWithTickets() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now().with(DayOfWeek.MONDAY), startTime), accessToken);
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now().with(DayOfWeek.SUNDAY), startTime), accessToken);

    mvc.perform(get("/time-logs/week/hours")
            .param("date", LocalDate.now(clock).toString())
            .param("includeTickets", "true")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.dayName == 'Monday')].ticketDurations[*]", hasItem(
            hasEntry("duration", "1h 0m")
        )));
  }

  @Test
  void shouldGetHoursForMonth() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;
    final LocalDate firstDate = LocalDate.now().with(firstDayOfMonth());

    MvcResult initialResult = mvc.perform(get("/time-logs/month/hours")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode initialResponse = objectMapper.readTree(initialResult.getResponse().getContentAsString());
    String initialTotalHours = initialResponse.get("totalHours").asText();

    String initialDayDuration = "0h 0m";
    JsonNode items = initialResponse.get("items");
    for (JsonNode item : items) {
      if (item.get("date").asText().equals(firstDate.format(ISO_LOCAL_DATE))) {
        initialDayDuration = item.get("duration").asText();
        break;
      }
    }

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(firstDate, startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now().with(lastDayOfMonth()), startTime), accessToken);

    mvc.perform(get("/time-logs/month/hours")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalHours").value(getDurationSum(initialTotalHours, "2h 0m")))
        .andExpect(jsonPath("$.items", hasItem(allOf(
            hasEntry("duration", getDurationSum(initialDayDuration, "1h 0m")),
            hasEntry("date", timeLog1.getDate().format(ISO_LOCAL_DATE))
        ))));
  }

  @Test
  void shouldUpdateTimeLog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime.plusHours(2)), accessToken);
    TimeLogEntity cloneForCompare = TimeLogApiTestUtils.clone(timeLog1);

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);

    TimeLogUpdateRequest request = new TimeLogUpdateRequest(timeLog1.getDate(), "NEW-1", timeLog1.getStartTime(), null, Collections.emptyList());

    mvc.perform(put("/time-logs/{id}", timeLog1.getId())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ticket").value(request.getTicket()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", not(matchTimeLog(cloneForCompare))))
        .andExpect(jsonPath("$.items", matchTimeLogUpdateDto(request)));

    mvc.perform(get("/time-logs/{id}", timeLog2.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.endTime").exists());
  }

  @Test
  void shouldSetGroupDescriptionForOneTimeLog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogSetGroupDescrRequest request = new TimeLogSetGroupDescrRequest(List.of(timeLog1.getId()), "New description");

    mvc.perform(patch("/time-logs/set-group-description")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));
  }

  @Test
  void shouldSetGroupDescriptionForManyTimeLogs() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime.plusHours(2)), accessToken);
    TimeLogSetGroupDescrRequest request = new TimeLogSetGroupDescrRequest(List.of(timeLog1.getId(), timeLog2.getId()), "New description");

    mvc.perform(patch("/time-logs/set-group-description")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));

    mvc.perform(get("/time-logs/{id}", timeLog2.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));
  }


  @Test
  void shouldDeleteTimeLog() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);

    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessToken);

    mvc.perform(delete("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog1))));

    int newSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessToken);
    assertThat(initialSize - 1).isEqualTo(newSize);
  }

  @Test
  void shouldChangeDateToNextDay() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogChangeDateRequest request = new TimeLogChangeDateRequest(true);

    mvc.perform(patch("/time-logs/{timeLogId}/change-date", timeLog1.getId())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value(LocalDate.now().plusDays(1).format(ISO_LOCAL_DATE)));
  }

  @Test
  void shouldChangeDateToPrevDay() throws Exception {
    final LocalTime startTime = SyncJiraUtils.DEFAULT_WORKLOG_START_TIME;

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), startTime), accessToken);
    TimeLogChangeDateRequest request = new TimeLogChangeDateRequest(false);

    mvc.perform(patch("/time-logs/{timeLogId}/change-date", timeLog1.getId())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value(LocalDate.now().minusDays(1).format(ISO_LOCAL_DATE)));
  }

  @Test
  void shouldGetNotFoundPassingNonExistingIdWhileDeleting() throws Exception {
    mvc.perform(delete("/time-logs/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Time log with such id does not exist"));
  }
}