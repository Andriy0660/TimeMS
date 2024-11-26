package com.example.timecraft.domain.auth;

import java.time.Clock;
import java.time.LocalDate;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.timecraft.config.ApiTest;
import com.example.timecraft.domain.auth.dto.AuthLogInRequest;
import com.example.timecraft.domain.auth.dto.AuthSignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ApiTest
public class AuthApiTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Clock clock;

  @Test
  void shouldSignUpUser() throws Exception {
    final String email = Instancio.of(String.class).create();
    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signup")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void shouldNotSignUpUserWhenEmailAlreadyExists() throws Exception {
    final String email = Instancio.of(String.class).create();
    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signup")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    mvc.perform(post("/auth/signup")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldLogInUser() throws Exception {
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

    final String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    assertThat(accessToken).isNotEqualTo(null);
  }

  @Test
  void shouldNotLogInWhenUserDoesNotExist() throws Exception {
    final String email = Instancio.of(String.class).create();
    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signup")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    final AuthLogInRequest logInRequest = new AuthLogInRequest("wrong email", signUpRequest.getPassword());
    mvc.perform(post("/auth/login")
            .content(objectMapper.writeValueAsString(logInRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldNotLogInWhenInvalidPassword() throws Exception {
    final String email = Instancio.of(String.class).create();
    final AuthSignUpRequest signUpRequest = new AuthSignUpRequest("someName", "lastNAme", email, "pass42243123");
    mvc.perform(post("/auth/signup")
            .content(objectMapper.writeValueAsString(signUpRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    final AuthLogInRequest logInRequest = new AuthLogInRequest(signUpRequest.getEmail(), "wrong password");
    mvc.perform(post("/auth/login")
            .content(objectMapper.writeValueAsString(logInRequest))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldLogOut() throws Exception {
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

    final String accessToken = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");

    final LocalDate startDate = LocalDate.now(clock);
    final LocalDate endDate = startDate.plusDays(1);
    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isOk());

    mvc.perform(post("/auth/logout").header("Authorization", accessToken)).andExpect(status().isOk());
    mvc.perform(get("/time-logs")
            .param("startDate", startDate.toString())
            .param("endDate", endDate.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", accessToken))
        .andExpect(status().isUnauthorized());
  }
}
