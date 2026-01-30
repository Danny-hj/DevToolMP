package com.devtoolmp.repository;

import com.devtoolmp.entity.ViewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ViewRecordRepository extends JpaRepository<ViewRecord, Long> {

    List<ViewRecord> findByToolIdAndCreatedAtBetween(Long toolId, LocalDateTime start, LocalDateTime end);
}
