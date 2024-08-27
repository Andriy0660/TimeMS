package com.example.timecraft.domain.timelog;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.timecraft.config.TestPostgresContainerConfiguration;
import com.example.timecraft.domain.timelog.dto.TimeLogCreateRequest;
import com.example.timecraft.domain.timelog.persistence.TimeLogEntity;
import com.example.timecraft.domain.timelog.persistence.TimeLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.example.timecraft.domain.timelog.utils.TimeLogApiTestUtils.createTimeLogEntity;
import static com.example.timecraft.domain.timelog.utils.TimeLogApiTestUtils.getSize;
import static com.example.timecraft.domain.timelog.utils.TimeLogApiTestUtils.matchTimeLog;
import static java.time.temporal.TemporalAdjusters.firstDayOfNextMonth;
import static java.time.temporal.TemporalAdjusters.next;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestPostgresContainerConfiguration.class)
@SpringBootTest
class TimeLogApiTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Clock clock;

  @Autowired
  private TimeLogRepository timeLogRepository;

  @Test
  void shouldReturnTimeLogsForDayMode() throws Exception {
    TimeLogEntity timeLog1 = createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog2 = createTimeLogEntity(LocalDate.now(clock).plusDays(1), LocalTime.of(2, 0, 0));
    TimeLogEntity timeLog3 = createTimeLogEntity(LocalDate.now(clock).plusDays(2), LocalTime.of(10, 0, 0));

    timeLog1 = timeLogRepository.save(timeLog1);
    timeLog2 = timeLogRepository.save(timeLog2);
    timeLog3 = timeLogRepository.save(timeLog3);


    mvc.perform(get("/time-logs")
            .param("mode", "Day")
            .param("date", LocalDate.now(clock).toString())
            .param("offset", "3")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldReturnTimeLogsForWeekMode() throws Exception {
    TimeLogEntity timeLog1 = createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog2 = createTimeLogEntity(LocalDate.now(clock).with(next(DayOfWeek.MONDAY)), LocalTime.of(2, 0, 0));
    TimeLogEntity timeLog3 = createTimeLogEntity(LocalDate.now(clock).with(next(DayOfWeek.TUESDAY)), LocalTime.of(10, 0, 0));

    timeLog1 = timeLogRepository.save(timeLog1);
    timeLog2 = timeLogRepository.save(timeLog2);
    timeLog3 = timeLogRepository.save(timeLog3);

    mvc.perform(get("/time-logs")
            .param("mode", "Week")
            .param("date", LocalDate.now(clock).toString())
            .param("offset", "3")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldReturnTimeLogsForMonthMode() throws Exception {
    TimeLogEntity timeLog1 = createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    TimeLogEntity timeLog2 = createTimeLogEntity(LocalDate.now(clock).with(firstDayOfNextMonth()), LocalTime.of(2, 0, 0));
    TimeLogEntity timeLog3 = createTimeLogEntity(LocalDate.now(clock).with(firstDayOfNextMonth()).plusDays(1),
        LocalTime.of(10, 0, 0));

    timeLog1 = timeLogRepository.save(timeLog1);
    timeLog2 = timeLogRepository.save(timeLog2);
    timeLog3 = timeLogRepository.save(timeLog3);


    mvc.perform(get("/time-logs")
            .param("mode", "Week")
            .param("date", LocalDate.now(clock).toString())
            .param("offset", "3")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog1)))
        .andExpect(jsonPath("$.items", matchTimeLog(timeLog2)))
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog3))));
  }

  @Test
  void shouldGetBadRequestWhenInvalidMode() throws Exception {
    mvc.perform(get("/time-logs")
            .param("mode", "invalid")
            .param("date", LocalDate.now(clock).toString())
            .param("offset", "3")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid time mode"));
  }

  @Test
  void shouldGetBadRequestWhenInvalidType() throws Exception {
    mvc.perform(get("/time-logs")
            .param("mode", "Day")
            .param("date", "invalid-type")
            .param("offset", "0")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Invalid argument type of parameter: date"));
  }

  @Test
  void shouldCreateTimeLog() throws Exception {
    TimeLogEntity timeLog1 = createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    timeLog1 = timeLogRepository.save(timeLog1);

    int initialSize = getSize(mvc, "Day", LocalDate.now(clock), 3);

    TimeLogCreateRequest request = new TimeLogCreateRequest("TMC-1", LocalDate.now(clock), LocalTime.of(9, 30, 0), "some descr");

    mvc.perform(post("/time-logs")
            .content(objectMapper.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.conflicted").value(true));

    int newSize = getSize(mvc, "Day", LocalDate.now(clock), 3);
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
    TimeLogEntity timeLog1 = createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    timeLog1 = timeLogRepository.save(timeLog1);
    mvc.perform(get("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ticket").value(timeLog1.getTicket()))
        .andExpect(jsonPath("$.description").value(timeLog1.getDescription()));
  }

  @Test
  void shouldGetNotFoundPassingNonExistingId() throws Exception {
    mvc.perform(get("/time-logs/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Time log with such id does not exist"));
  }

  @Test
  void shouldDeleteTimeLog() throws Exception {
    TimeLogEntity timeLog1 = createTimeLogEntity(LocalDate.now(clock), LocalTime.of(9, 0, 0));
    timeLog1 = timeLogRepository.save(timeLog1);

    int initialSize = getSize(mvc, "Day", LocalDate.now(clock), 3);

    mvc.perform(delete("/time-logs/{id}", timeLog1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(get("/time-logs")
            .param("mode", "Day")
            .param("date", LocalDate.now(clock).toString())
            .param("offset", "3")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", not(matchTimeLog(timeLog1))));

    int newSize = getSize(mvc, "Day", LocalDate.now(clock), 3);
    assertEquals(initialSize - 1, newSize);
  }

  @Test
  void shouldGetNotFoundPassingNonExistingIdWhileDeleting() throws Exception {
    mvc.perform(delete("/time-logs/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("Time log with such id does not exist"));
  }
}