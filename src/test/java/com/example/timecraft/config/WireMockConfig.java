package com.example.timecraft.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.github.tomakehurst.wiremock.WireMockServer;

@TestConfiguration
public class WireMockConfig {

  @Bean
  public WireMockServer wireMockServer() {
    WireMockServer wireMockServer = new WireMockServer(9999);
    wireMockServer.start();
    return wireMockServer;
  }

}