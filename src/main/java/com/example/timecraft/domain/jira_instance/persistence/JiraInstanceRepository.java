package com.example.timecraft.domain.jira_instance.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JiraInstanceRepository extends JpaRepository<JiraInstanceEntity, Long> {
}
