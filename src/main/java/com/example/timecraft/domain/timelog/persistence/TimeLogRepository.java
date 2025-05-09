package com.example.timecraft.domain.timelog.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLogEntity, Long> {
  List<TimeLogEntity> findAllByDateIs(LocalDate day);

  @Query("""
      SELECT t FROM TimeLogEntity t WHERE
      (t.date = :currentDay AND (t.startTime >= :startTime OR t.startTime IS NULL))
      OR (t.date = :nextDay AND t.startTime < :startTime)
      OR (t.date > :currentDay AND t.date < :nextDay)
     """)
  List<TimeLogEntity> findAllInRange(
      @Param("currentDay") LocalDate currentDay,
      @Param("nextDay") LocalDate nextDay,
      @Param("startTime") LocalTime startTime
  );

  @Query("""
      SELECT t FROM TimeLogEntity t WHERE
      ((t.date = :day AND (t.startTime >= :startTime OR t.startTime IS NULL))
      OR (t.date = :nextDay AND t.startTime < :startTime))
      AND t.ticket = :ticket
     """)
  List<TimeLogEntity> findAllByDateAndTicket(
      @Param("day") LocalDate day,
      @Param("nextDay") LocalDate nextDay,
      @Param("startTime") LocalTime startTime,
      @Param("ticket") String ticket);

  List<TimeLogEntity> findAllByEndTimeIsNull();

  @Query("""
      SELECT t FROM TimeLogEntity t WHERE
      ((t.date = :day AND (t.startTime >= :startTime OR t.startTime IS NULL))
      OR (t.date = :nextDay AND t.startTime < :startTime))
      AND t.description = :description
     """)
  List<TimeLogEntity> findAllByDateAndDescription(
      @Param("day") LocalDate day,
      @Param("nextDay") LocalDate nextDay,
      @Param("startTime") LocalTime startTime,
      @Param("description") String description
  );
}
