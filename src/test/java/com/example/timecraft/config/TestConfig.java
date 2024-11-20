package com.example.timecraft.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

import com.example.timecraft.core.config.AppProperties;
import com.github.tomakehurst.wiremock.WireMockServer;

@TestConfiguration
public class TestConfig {
  @Autowired
  private WebApplicationContext webApplicationContext;

  @Bean
  @ServiceConnection
  public PostgreSQLContainer<?> postgreSQLContainer() {
    return new PostgreSQLContainer<>("postgres:16.3");
  }

  @Bean
  public MockMvc mockMvc() {
    return MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
  }

  @Bean
  public WireMockServer wireMockServer(final AppProperties properties) {
    final WireMockServer wireMockServer = new WireMockServer(0);
    wireMockServer.start();
    int port = wireMockServer.port();
    properties.getJira().setUrl("http://localhost:" + port);
    return wireMockServer;
  }

}
