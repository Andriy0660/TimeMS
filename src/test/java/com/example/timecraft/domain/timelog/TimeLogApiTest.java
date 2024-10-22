package com.example.timecraft.domain.timelog;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.timecraft.config.IntegrationTest;
import com.example.timecraft.config.MockMvcConfig;
import com.example.timecraft.config.TestPostgresContainerConfiguration;
import com.example.timecraft.core.config.AppProperties;
import com.example.timecraft.domain.timelog.dto.TimeLogChangeDateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateFromWorklogRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogImportRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogSetGroupDescrRequest;
import com.example.timecraft.domain.timelog.dto.TimeLogUpdateRequest;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.service.TestTimeLogService;
import com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.example.timecraft.domain.sync.jira.util.SyncJiraUtils.defaultWorklogStartTime;
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class TimeLogApiTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Clock clock;

  @Autowired
  private AppProperties props;

  @Autowired
  private TestTimeLogService timeLogService;

  @Test
  void shouldReturnTimeLogsForDayMode() throws Exception {
    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).plusDays(1),
        LocalTime.of(2, 0, 0)));
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).plusDays(2),
        LocalTime.of(10, 0, 0)));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldReturnTimeLogsForWeekMode() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).with(next(DayOfWeek.MONDAY)),
        LocalTime.of(2, 0, 0)));
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).plusDays(8),
        LocalTime.of(10, 0, 0)));

    LocalDate startDate = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endDate = LocalDate.now(clock).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldReturnTimeLogsForMonthMode() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).with(firstDayOfNextMonth()),
        LocalTime.of(2, 0, 0)));
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock).with(firstDayOfNextMonth()).plusDays(1),
        LocalTime.of(10, 0, 0)));

    LocalDate startDate = LocalDate.now(clock).with(TemporalAdjusters.firstDayOfMonth());
    LocalDate endDate = LocalDate.now(clock).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
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
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid argument type of parameter: startDate"));
  }

  @Test
  void shouldCreateTimeLog() throws Exception {
    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    TimeLogCreateRequest request = new TimeLogCreateRequest("TMC-1", LocalDate.now(clock), defaultWorklogStartTime, "some descr");

    mvc.perform(post("/time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int newSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    assertEquals(initialSize + 1, newSize);
  }

  @Test
  void shouldCreateTimeLogFromWorklog() throws Exception {
    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    TimeLogCreateFromWorklogRequest request = new TimeLogCreateFromWorklogRequest("TST-2", LocalDate.now(clock), defaultWorklogStartTime, "some descr", 3600);

    mvc.perform(post("/time-logs/fromWorklog")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    int newSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    assertEquals(initialSize + 1, newSize);
  }

  @Test
  void shouldGetBadRequestProvidingNotCreateRequest() throws Exception {
    mvc.perform(post("/time-logs")
            .content(objectMapper.writeValueAsString(null))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldGetTimeLog() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ticket").value(timeLog1.getTicket()))
        .andExpect(jsonPath("$.description").value(timeLog1.getDescription()));
  }

  @Test
  void shouldGetConfig() throws Exception {
    mvc.perform(get("/time-logs/config")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isJiraSyncingEnabled").value(props.getConfig().getIsJiraSyncingEnabled()))
        .andExpect(jsonPath("$.offset").value(props.getConfig().getOffset()))
        .andExpect(jsonPath("$.startHourOfWorkingDay").value(props.getConfig().getStartHourOfWorkingDay()))
        .andExpect(jsonPath("$.endHourOfWorkingDay").value(props.getConfig().getEndHourOfWorkingDay()));
  }

  @Test
  void shouldGetNotFoundPassingNonExistingId() throws Exception {
    mvc.perform(get("/time-logs/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
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

    timeLogService.saveTimeLog(createTimeLogCreateRequest(timeLogDto1.getDate(), timeLogDto1.getStartTime(), timeLogDto1.getTicket(), timeLogDto1.getDescription()));
    timeLogService.saveTimeLog(createTimeLogCreateRequest(timeLogDto2.getDate(), timeLogDto2.getStartTime(), timeLogDto2.getTicket(), timeLogDto2.getDescription()));

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
    int initialSize = getSize(mvc, startDate, endDate);

    mvc.perform(post("/time-logs/importTimeLogs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    int newSize = getSize(mvc, startDate, endDate);

    assertEquals(initialSize + 2, newSize);

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLogMergeDto(timeLogDto3)))
        .andExpect(jsonPath("$.items", matchTimeLogMergeDto(timeLogDto4)));

  }

  @Test
  void shouldDivideTimeLog() throws Exception {
    TimeLogEntity timeLog = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(11, 0, 0)), LocalTime.of(5, 0));
    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    mvc.perform(post("/time-logs/divide/{timeLogId}", timeLog.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    int newSizeOfThisDay = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    int sizeOfNextDay = getSize(mvc, LocalDate.now(clock).plusDays(1), LocalDate.now(clock).plusDays(1));
    assertEquals(initialSize + 1, newSizeOfThisDay + sizeOfNextDay);
  }

  @Test
  void shouldGetHoursForWeek() throws Exception {
    LocalDate monday = LocalDate.now(clock).with(DayOfWeek.MONDAY);
    LocalDate sunday = LocalDate.now().with(DayOfWeek.SUNDAY);

    MvcResult initialResult = mvc.perform(get("/time-logs/hoursForWeek")
            .param("date", LocalDate.now(clock).toString())
            .param("includeTickets", "false")
            .contentType(MediaType.APPLICATION_JSON))
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

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(monday, defaultWorklogStartTime));
    TimeLogEntity timeLog12 = timeLogService.saveTimeLog(createTimeLogCreateRequest(monday, defaultWorklogStartTime.plusMinutes(30)));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(sunday, defaultWorklogStartTime));

    String expectedMondayDuration = getDurationSum(initialMondayDuration, "2h 0m");
    String expectedSundayDuration = getDurationSum(initialSundayDuration, "1h 0m");

    mvc.perform(get("/time-logs/hoursForWeek")
            .param("date", LocalDate.now(clock).toString())
            .param("includeTickets", "false")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.dayName == 'Monday' && @.duration == '" + expectedMondayDuration + "' && @.conflicted == true)]").exists())
        .andExpect(jsonPath("$.items[?(@.dayName == 'Sunday' && @.duration == '" + expectedSundayDuration + "' && @.conflicted == " +
            (initialSundayConflicted || false) + ")]").exists());
  }

  @Test
  void shouldGetHoursForWeekWithTickets() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now().with(DayOfWeek.MONDAY), defaultWorklogStartTime));
    TimeLogEntity timeLog3 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now().with(DayOfWeek.SUNDAY), defaultWorklogStartTime));

    mvc.perform(get("/time-logs/hoursForWeek")
            .param("date", LocalDate.now(clock).toString())
            .param("includeTickets", "true")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items[?(@.dayName == 'Monday')].ticketDurations[*]", hasItem(
            hasEntry("duration", "1h 0m")
        )));
  }

  @Test
  void shouldGetHoursForMonth() throws Exception {
    LocalDate firstDate = LocalDate.now().with(firstDayOfMonth());

    MvcResult initialResult = mvc.perform(get("/time-logs/hoursForMonth")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON))
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

    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(firstDate, defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now().with(lastDayOfMonth()), defaultWorklogStartTime));

    mvc.perform(get("/time-logs/hoursForMonth")
            .param("date", LocalDate.now(clock).toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalHours").value(getDurationSum(initialTotalHours, "2h 0m")))
        .andExpect(jsonPath("$.items", hasItem(allOf(
            hasEntry("duration", getDurationSum(initialDayDuration, "1h 0m")),
            hasEntry("date", timeLog1.getDate().format(ISO_LOCAL_DATE))
        ))));
  }

  @Test
  void shouldUpdateTimeLog() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime.plusHours(2)), null);
    TimeLogEntity cloneForCompare = TimeLogApiTestUtils.clone(timeLog1);

    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);

    TimeLogUpdateRequest request = new TimeLogUpdateRequest(timeLog1.getDate(), "NEW-1", timeLog1.getStartTime(), null);

    mvc.perform(put("/time-logs/{id}", timeLog1.getId())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ticket").value(request.getTicket()));

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", not(matchTimeLog(cloneForCompare))))
        .andExpect(jsonPath("$.items", matchTimeLogUpdateDto(request)));

    mvc.perform(get("/time-logs/{id}", timeLog2.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.endTime").exists());
  }

  @Test
  void shouldSetGroupDescriptionForOneTimeLog() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogSetGroupDescrRequest request = new TimeLogSetGroupDescrRequest(List.of(timeLog1.getId()), "New description");

    mvc.perform(patch("/time-logs/setGroupDescription")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));
  }

  @Test
  void shouldSetGroupDescriptionForManyTimeLogs() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogEntity timeLog2 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime.plusHours(2)));
    TimeLogSetGroupDescrRequest request = new TimeLogSetGroupDescrRequest(List.of(timeLog1.getId(), timeLog2.getId()), "New description");

    mvc.perform(patch("/time-logs/setGroupDescription")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));

    mvc.perform(get("/time-logs/{id}", timeLog2.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value(request.getDescription()));
  }


  @Test
  void shouldDeleteTimeLog() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    LocalDate startDate = LocalDate.now(clock);
    LocalDate endDate = LocalDate.now(clock).plusDays(1);

    int initialSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));

    mvc.perform(delete("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog1))));

    int newSize = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1));
    assertEquals(initialSize - 1, newSize);
  }

  @Test
  void shouldChangeDateToNextDay() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogChangeDateRequest request = new TimeLogChangeDateRequest(true);

    mvc.perform(patch("/time-logs/{timeLogId}/changeDate", timeLog1.getId())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value(LocalDate.now().plusDays(1).format(ISO_LOCAL_DATE)));
  }

  @Test
  void shouldChangeDateToPrevDay() throws Exception {
    TimeLogEntity timeLog1 = timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), defaultWorklogStartTime));
    TimeLogChangeDateRequest request = new TimeLogChangeDateRequest(false);

    mvc.perform(patch("/time-logs/{timeLogId}/changeDate", timeLog1.getId())
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.date").value(LocalDate.now().minusDays(1).format(ISO_LOCAL_DATE)));
  }

  @Test
  void shouldGetNotFoundPassingNonExistingIdWhileDeleting() throws Exception {
    mvc.perform(delete("/time-logs/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Time log with such id does not exist"));
  }
}