package com.devtoolmp.repository;

import com.devtoolmp.entity.TelemetryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<TelemetryData, Long> {

    List<TelemetryData> findByToolIdAndCreatedAtBetween(Long toolId, LocalDateTime start, LocalDateTime end);

    List<TelemetryData> findByEventTypeAndCreatedAtBetween(String eventType, LocalDateTime start, LocalDateTime end);
}
