package com.example.timecraft.domain.worklog.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<WorklogEntity, Long> {

  @Query("SELECT t FROM WorklogEntity t WHERE " +
      "(t.date = :currentDay AND (t.startTime >= :startTime OR t.startTime IS NULL)) " +
      "OR (t.date = :nextDay AND t.startTime < :startTime) " +
      "OR (t.date > :currentDay AND t.date < :nextDay)")
  List<WorklogEntity> findAllInRange(@Param("currentDay") LocalDate currentDay,
                                     @Param("nextDay") LocalDate nextDay,
                                     @Param("startTime") LocalTime startTime);

  List<WorklogEntity> findAllByTicket(String ticket);
}
