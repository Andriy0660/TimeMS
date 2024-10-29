package com.example.timecraft.domain.sync.upwork.persistence;

import java.time.LocalDate;

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
@Table(name = "upwork_sync_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class UpworkSyncInfoEntity {
  @Id
  @Column(name = "date")
  @ToString.Include
  private LocalDate date;

  @Column(name = "time_spent_seconds")
  @ToString.Include
  private Integer timeSpentSeconds;
}
