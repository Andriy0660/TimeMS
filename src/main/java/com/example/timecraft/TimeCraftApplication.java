package com.example.timecraft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;

@SpringBootApplication(exclude = {LiquibaseAutoConfiguration.class})
public class TimeCraftApplication {

  public static void main(String[] args) {
    SpringApplication.run(TimeCraftApplication.class, args);
  }

}
