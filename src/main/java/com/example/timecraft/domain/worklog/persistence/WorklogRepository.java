package com.example.timecraft.domain.worklog.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<WorklogEntity, Long> {

  List<WorklogEntity> findAllByDateAndTicket(LocalDate date, String ticket);

  List<WorklogEntity> findAllByDate(LocalDate date);

  List<WorklogEntity> findAllByTicket(String ticket);
}
