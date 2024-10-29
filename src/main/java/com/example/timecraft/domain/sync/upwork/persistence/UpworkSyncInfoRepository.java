package com.example.timecraft.domain.sync.upwork.persistence;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpworkSyncInfoRepository extends JpaRepository<UpworkSyncInfoEntity, LocalDate> {

}
