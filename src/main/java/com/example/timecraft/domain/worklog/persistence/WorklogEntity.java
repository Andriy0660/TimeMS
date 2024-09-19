package com.example.timecraft.domain.worklog.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "worklog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class WorklogEntity {

  @Id
  @Column(name = "id")
  @ToString.Include
  private Long id;

  @Column(name = "author")
  @ToString.Include
  private String author;

  @Column(name = "ticket")
  @ToString.Include
  private String ticket;

  @Column(name = "date")
  @ToString.Include
  private LocalDate date;

  @Column(name = "start_time")
  @ToString.Include
  private LocalTime startTime;

  @Column(name = "comment")
  @ToString.Include
  private String comment;

  @Column(name = "time_spent_seconds")
  @ToString.Include
  private Integer timeSpentSeconds;

  @Column(name = "updated")
  @ToString.Include
  private LocalDateTime updated;

}
