package com.example.timecraft.domain.worklog.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<WorklogEntity, Long> {
  List<WorklogEntity> findAllByDateIs(LocalDate day);

}
