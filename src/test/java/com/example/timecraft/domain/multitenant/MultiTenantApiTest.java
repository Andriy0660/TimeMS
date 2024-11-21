package com.example.timecraft.domain.multitenant;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.config.ApiTest;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.example.timecraft.domain.multitenant.util.MultiTenantUtils;
import com.example.timecraft.domain.timelog.service.TestTimeLogClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.createTimeLogCreateRequest;
import static com.example.timecraft.domain.timelog.util.TimeLogApiTestUtils.getSize;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
public class MultiTenantApiTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private EntityManager em;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TestTimeLogClient timeLogService;

  @Autowired
  private Clock clock;

  @Test
  void shouldCreateSchema() throws Exception {
    final String email = Instancio.of(String.class).create();
    final String schemaName = MultiTenantUtils.generateSchemaNameFromEmail(email);

    boolean schemaExists = schemaExists(schemaName);
    assertThat(schemaExists).isFalse();

    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signUp")
        .content(objectMapper.writeValueAsString(signUpRequest))
        .contentType(MediaType.APPLICATION_JSON));

    schemaExists = schemaExists(schemaName);
    assertThat(schemaExists).isTrue();
  }

  private boolean schemaExists(String schemaName) {
    Query query = em.createNativeQuery(
        "SELECT schema_name FROM information_schema.schemata WHERE schema_name = :schemaName");
    query.setParameter("schemaName", schemaName);
    return !query.getResultList().isEmpty();
  }

  @Test
  void shouldGetTimeLogsForSpecificUser() throws Exception {
    final AuthSignUpRequest signUpRequestUser1 = new AuthSignUpRequest("user", "1", "user1@gmail.com", "user1");
    final AuthSignUpRequest signUpRequestUser2 = new AuthSignUpRequest("user", "2", "user2@gmail.com", "user2");

    mvc.perform(post("/auth/signUp")
        .content(objectMapper.writeValueAsString(signUpRequestUser1))
        .contentType(MediaType.APPLICATION_JSON));

    mvc.perform(post("/auth/signUp")
        .content(objectMapper.writeValueAsString(signUpRequestUser2))
        .contentType(MediaType.APPLICATION_JSON));

    final AuthLogInRequest logInRequest1 = new AuthLogInRequest(signUpRequestUser1.getEmail(), signUpRequestUser1.getPassword());
    final MvcResult result1 = mvc.perform(post("/auth/logIn")
            .content(objectMapper.writeValueAsString(logInRequest1))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    final String accessTokenUser1 = JsonPath.read(result1.getResponse().getContentAsString(), "$.accessToken");
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(9, 0)), accessTokenUser1);
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(11, 0)), accessTokenUser1);

    final AuthLogInRequest logInRequest2 = new AuthLogInRequest(signUpRequestUser2.getEmail(), signUpRequestUser2.getPassword());
    final MvcResult result2 = mvc.perform(post("/auth/logIn")
            .content(objectMapper.writeValueAsString(logInRequest2))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk()).andReturn();

    final String accessTokenUser2 = JsonPath.read(result2.getResponse().getContentAsString(), "$.accessToken");
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(13, 0)), accessTokenUser2);
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(15, 0)), accessTokenUser2);
    timeLogService.saveTimeLog(createTimeLogCreateRequest(LocalDate.now(clock), LocalTime.of(17, 0)), accessTokenUser2);

    int size1 = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessTokenUser1);
    Assertions.assertThat(size1).isEqualTo(2);

    int size2 = getSize(mvc, LocalDate.now(clock), LocalDate.now(clock).plusDays(1), accessTokenUser2);
    Assertions.assertThat(size2).isEqualTo(3);
  }

}
