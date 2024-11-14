package com.example.timecraft.domain.external_timelog.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalTimeLogRepository extends JpaRepository<ExternalTimeLogEntity, Long> {

  List<ExternalTimeLogEntity> findAllByDate(LocalDate date);

  List<ExternalTimeLogEntity> findAllByDateAndDescription(LocalDate date, String description);
}
