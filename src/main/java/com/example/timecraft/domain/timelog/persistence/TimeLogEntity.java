package com.example.timecraft.domain.timelog.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "timelogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class TimeLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @ToString.Include
  private Long id;

  @Column(name = "ticket_number")
  @ToString.Include
  private String ticket;

  @Column(name = "description")
  @ToString.Include
  private String description;

  @Column(name = "date")
  @ToString.Include
  private LocalDate date;

  @Column(name = "start_time")
  @ToString.Include
  private LocalTime startTime;

  @Column(name = "end_time")
  @ToString.Include
  private LocalTime endTime;

}
