package com.example.timecraft.domain.logEntry.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends JpaRepository<LogEntryEntity, Long> {
  List<LogEntryEntity> findAllByEndTimeIsNull();
}
