package com.example.timecraft.domain.worklog.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorklogRepository extends JpaRepository<WorklogEntity, Long> {
}
