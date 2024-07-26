package com.example.timecraft.domain.timelog.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLogEntity, Long> {
  List<TimeLogEntity> findAllByDateIs(LocalDate day);
  List<TimeLogEntity> findAllByDateBetween(LocalDate start, LocalDate end);
  List<TimeLogEntity> findAllByEndTimeIsNull();
}
