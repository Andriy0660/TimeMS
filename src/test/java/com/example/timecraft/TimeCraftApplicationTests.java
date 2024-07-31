package com.example.timecraft;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.timecraft.config.TestPostgresContainerConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(TestPostgresContainerConfiguration.class)
class TimeCraftApplicationTests {

  @Test
  void contextLoads() {
  }

}
